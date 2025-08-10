use std::ptr;

use jni::{AttachGuard, objects::JClass};
use notify_rust::Notification;

use crate::DEBUG_MODE;

/// # Safety
/// This function is unsafe because it deals with raw JNI pointers and operations.
pub unsafe fn load_class_bytes<'a>(
    jni: &'a mut AttachGuard,
    class_name: &str,
    class_bytes: &[u8],
) -> jni::errors::Result<JClass<'a>> {
    let thread_class = jni.find_class("java/lang/Thread")?;
    let current_thread_obj = jni
        .call_static_method(thread_class, "currentThread", "()Ljava/lang/Thread;", &[])?
        .l()?;
    let class_loader_obj = jni
        .call_method(
            current_thread_obj,
            "getContextClassLoader",
            "()Ljava/lang/ClassLoader;",
            &[],
        )?
        .l()?;
    jni.define_class(class_name, &class_loader_obj, class_bytes)
}

pub fn create_notification(message: &str) {
    let debug_mode = unsafe { (*ptr::addr_of!(DEBUG_MODE)).get().unwrap() };
    
    if *debug_mode {
        Notification::new()
            .summary("Badlion tweaks")
            .body(message)
            .show()
            .unwrap();
    }
}
