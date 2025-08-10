use jni::{
    JNIEnv,
    sys::{JNIEnv as RawJNIEnv, jclass, jint, jlong, jstring},
};
use once_cell::sync::Lazy;
use retour::GenericDetour;
use std::{
    mem, ptr,
    sync::atomic::{AtomicBool, Ordering},
};
use thiserror::Error;
use windows_sys::Win32::System::LibraryLoader::{GetModuleHandleA, GetProcAddress};

use crate::{thread_helpers::ThreadSuspender, tweaks::load_tweaks, utils::create_notification};

#[derive(Debug, Error)]
pub enum HookError {
    #[error("Cannot find module {0}")]
    NoModule(&'static str),
    #[error("Cannot find function {0} in module")]
    NoFunc(&'static str),
    #[error("Detour init failed: {0}")]
    DetourInit(String),
    #[error("Suspend threads failed: {0}")]
    Suspend(String),
}

type NglClearRaw =
    unsafe extern "system" fn(env: *mut RawJNIEnv, jclazz: jclass, mask: jint, ptr: jlong);

static NGLCLEAR_HOOK: Lazy<Result<GenericDetour<NglClearRaw>, String>> = Lazy::new(|| unsafe {
    let target_ptr = find_symbol_raw(b"lwjgl64.dll\0", b"Java_org_lwjgl_opengl_GL11_nglClear\0")
        .map_err(|e| e.to_string())?;

    let target: NglClearRaw = mem::transmute::<*const (), NglClearRaw>(target_ptr);

    let detour_hook: NglClearRaw = hooked_ngl_clear;

    let _ = ThreadSuspender::new();

    let detour = GenericDetour::new(target, detour_hook).map_err(|e| e.to_string())?;
    Ok(detour)
});

static TWEAKS_LOADED_ONCE: AtomicBool = AtomicBool::new(false);

fn find_symbol_raw(module: &[u8], symbol: &[u8]) -> Result<*const (), HookError> {
    unsafe {
        let hmod = GetModuleHandleA(module.as_ptr());
        if hmod.is_null() {
            return Err(HookError::NoModule("module"));
        }
        let addr = GetProcAddress(hmod, symbol.as_ptr());
        match addr {
            Some(p) => {
                let raw = p as usize as *const ();
                Ok(raw)
            }
            None => Err(HookError::NoFunc("symbol")),
        }
    }
}

#[unsafe(no_mangle)]
pub extern "C" fn install_hook() {
    match &*NGLCLEAR_HOOK {
        Ok(_) => {
            println!("[+] nglClear detour enabled.");
        }
        Err(e) => {
            eprintln!("[!] Failed to enable nglClear hook: {e}");
        }
    }
}

unsafe extern "system" fn hooked_ngl_clear(
    env: *mut RawJNIEnv,
    _jclazz: jclass,
    _mask: jint,
    _ptr64: jlong,
) {
    if !TWEAKS_LOADED_ONCE.swap(true, Ordering::SeqCst) {
        if let Some(mut typed_env) = unsafe { JNIEnv::from_raw(env).ok() } {
            println!("[hook] nglClear reached -> loading tweaks once...");
            match unsafe { load_tweaks(&mut typed_env) } {
                Ok(_) => println!("[hook] tweaks loaded."),
                Err(e) => eprintln!("[hook] load_tweaks failed: {e}"),
            }
        } else {
            eprintln!("[hook] failed to build JNIEnv from raw pointer");
        }
    }

    // if let Ok(h) = &*NGLCLEAR_HOOK {
    //     let orig: &NglClearRaw = h();
    //     return orig(env, jclazz, mask, ptr64);
    // }

    eprintln!("[hook] trampoline missing; skipping original nglClear");
}

type GetCosmeticsRaw = unsafe extern "system" fn(
    env: *mut RawJNIEnv,
    thiz: jni::sys::jobject,
    arg: jstring,
) -> jstring;

static GET_COSMETICS_HOOK: Lazy<Result<GenericDetour<GetCosmeticsRaw>, String>> =
    Lazy::new(|| unsafe {
        let target_ptr = find_symbol_raw(
            b"badlion_electron.dll\0",
            b"Java_net_badlion_client_Wrapper_getAvailableCosmetics\0",
        )
        .map_err(|e| e.to_string())?;

        let target: GetCosmeticsRaw = mem::transmute::<*const (), GetCosmeticsRaw>(target_ptr);
        let detour_hook: GetCosmeticsRaw = hooked_get_available_cosmetics;
        let detour = GenericDetour::new(target, detour_hook).map_err(|e| e.to_string())?;

        let _ = ThreadSuspender::new();
        detour.enable().map_err(|e| e.to_string())?;
        Ok(detour)
    });

#[unsafe(no_mangle)]
pub extern "C" fn install_cosmetics_hook() {
    create_notification("Installing cosmetics hook");
    match &*GET_COSMETICS_HOOK {
        Ok(_) => {
            create_notification("Enabled cosmetics hook");
            println!("[+] getAvailableCosmetics detour enabled.")
        }
        Err(e) => eprintln!("[!] Failed to enable getAvailableCosmetics hook: {e}"),
    }
}

unsafe extern "system" fn hooked_get_available_cosmetics(
    env: *mut RawJNIEnv,
    _thiz: jni::sys::jobject,
    _arg: jstring,
) -> jstring {
    create_notification("Hooking method get_available_cosmetics");
    let json_txt = include_str!(concat!(
        env!("CARGO_MANIFEST_DIR"),
        "/resources/cosmetics.json"
    ));

    let result = build_cosmetics_json(&json_txt).unwrap_or_else(|e| {
        eprintln!("[hook] build_cosmetics_json error: {e}");
        create_notification("Failed to build cosmetics json");
        String::from("{\"cosmetics\":[]}")
    });

    if let Some(jni_env) = unsafe { JNIEnv::from_raw(env).ok() } {
        match jni_env.new_string(&result) {
            Ok(js) => js.into_raw(),
            Err(e) => {
                create_notification("Failed to create jstring (cosmetics json)");
                eprintln!("[hook] new_string failed: {e}");
                ptr::null_mut()
            }
        }
    } else {
        create_notification("failed to get JNIEnv (cosmetics)");
        eprintln!("[hook] JNIEnv from_raw failed");
        ptr::null_mut()
    }
}

fn build_cosmetics_json(input: &str) -> Result<String, serde_json::Error> {
    use serde_json::{Value, json};
    let v: Value = serde_json::from_str(input)?;

    let mut cosmetics = Vec::new();
    if let Some(obj) = v.get("registeredCosmetics").and_then(|x| x.as_object()) {
        for (key, val) in obj {
            if key.contains("NAMETAG") {
                continue;
            }
            if let Some(arr) = val.as_array() {
                for item in arr {
                    let cosmetic_id = item.get("cosmeticId").cloned().unwrap_or(Value::Null);
                    cosmetics.push(json!({
                        "cosmeticId": cosmetic_id,
                        "cosmeticType": key,
                        "active": false
                    }));
                }
            }
        }
    }
    let out = serde_json::json!({ "cosmetics": cosmetics });
    Ok(out.to_string())
}
