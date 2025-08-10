package org.cubewhy.badtweaks.tweaks.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.cubewhy.badtweaks.tweaks.Tweak;

import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CosmeticsTweak implements Tweak {
    @Override
    public void apply() throws Exception {
        Object wrapperInstance = getWrapperInstance();

        // get all available cosmetics
        JsonObject storeItems = getStoreItems(wrapperInstance);

        Gson gson = new Gson();

        Class<?> cosmeticClass = getCosmeticClass();

        List<Object> cosmeticsObjs = new ArrayList<>();
        storeItems.getAsJsonArray("cosmetics").forEach(c -> {
            JsonObject cosmetic = c.getAsJsonObject();
            cosmeticsObjs.add(gson.fromJson(cosmetic, cosmeticClass));
        });

        // get badlion instance
        Object badlionInstance = getBadlionInstance();

        // get badlionSettings field
        Object badlionSettings = getBadlionSettings(badlionInstance);

        // get cosmetics response manager?
        Object cosmeticsStore = getCosmeticsStore(badlionInstance);

        // get cosmetics field
        List<Object> cosmeticsList = getCosmeticsList(cosmeticsStore);

        // add cosmetics to badlion cosmeticsList
        cosmeticsList.clear();
        cosmeticsList.addAll(cosmeticsObjs);

        // add cosmetic names to favoriteCosmetics
        HashSet<String> set = getFavoriteCosmeticNameSet(badlionSettings);
        set.clear();
        set.addAll(cosmeticsList
                .stream().map(cos -> {
                    try {
                        return getCosmeticName(cos);
                    } catch (InvocationTargetException | IllegalAccessException | NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    }
                }).toList());

        // clear favoriteCosmeticsIds
        HashSet<String> favoriteCosmeticsIds = getFavoriteCosmeticsIdsField(badlionSettings);
        favoriteCosmeticsIds.clear();

        // refresh cosmetics
        refreshCosmetics(badlionSettings);
        refreshUi(badlionInstance);
    }

    private void refreshUi(Object badlionInstance) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> badlionClass = badlionInstance.getClass();
        Method methodRefreshUi = badlionClass.getDeclaredMethod("fB");
        methodRefreshUi.invoke(badlionInstance);
    }

    @SuppressWarnings("unchecked")
    private HashSet<String> getFavoriteCosmeticNameSet(Object badlionSettings) throws NoSuchFieldException, IllegalAccessException {
        // get the favoriteCosmetics field
        Class<?> badlionSettingsClass = badlionSettings.getClass();
        Field fieldFavoriteCosmetics = badlionSettingsClass.getDeclaredField("favoriteCosmetics");
        fieldFavoriteCosmetics.setAccessible(true);

        // get the set
        return (HashSet<String>) fieldFavoriteCosmetics.get(badlionSettings);
    }

    @SuppressWarnings("unchecked")
    private HashSet<String> getFavoriteCosmeticsIdsField(Object badlionSettings) throws NoSuchFieldException, IllegalAccessException {
        // get the favoriteCosmeticsIds field
        Class<?> badlionSettingsClass = badlionSettings.getClass();
        Field fieldFavoriteCosmetics = badlionSettingsClass.getDeclaredField("favoriteCosmeticsIds");
        fieldFavoriteCosmetics.setAccessible(true);

        // get the set
        return (HashSet<String>) fieldFavoriteCosmetics.get(badlionSettings);
    }

    private String getCosmeticName(Object cosmetic) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Class<?> cosmeticClass = cosmetic.getClass();
        Field nameField = cosmeticClass.getDeclaredField("name");
        nameField.setAccessible(true);
        // call the method
        return (String) nameField.get(cosmetic);
    }

    private void refreshCosmetics(Object badlionSettings) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Class<?> badlionSettingsClass = badlionSettings.getClass();
        Method methodRefreshCosmetics = badlionSettingsClass.getDeclaredMethod("gO");

        methodRefreshCosmetics.invoke(badlionSettings);
    }

    @SuppressWarnings("unchecked")
    private List<Object> getCosmeticsList(Object cosmeticsStoreInstance) throws NoSuchFieldException, IllegalAccessException {
        Class<?> cosmeticsStoreClass = cosmeticsStoreInstance.getClass();
        Field cosmeticsField = cosmeticsStoreClass.getDeclaredField("cosmetics");
        cosmeticsField.setAccessible(true);

        return (List<Object>) cosmeticsField.get(cosmeticsStoreInstance);
    }

    private Class<?> getCosmeticClass() throws ClassNotFoundException {
        return Class.forName("net.badlion.a.aCU");
    }

    private Object getCosmeticsStore(Object badlionInstance) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // it called eY
        Method methodGetUnknown = badlionInstance.getClass().getDeclaredMethod("eY");
        methodGetUnknown.setAccessible(true);
        Object unknownObject = methodGetUnknown.invoke(badlionInstance);

        Class<?> unknownObjectClass = unknownObject.getClass();
        // now get cosmetics store
        // it called bsi
        Method methodGetCosmeticsStore = unknownObjectClass.getDeclaredMethod("bsi");
        return methodGetCosmeticsStore.invoke(unknownObject);
    }

    private Object getBadlionInstance() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // get badlion class
        Class<?> wrapperClass = Class.forName("net.badlion.a.db");
        // get instance
        Method methodGetInstance = wrapperClass.getDeclaredMethod("getInstance");
        return methodGetInstance.invoke(null);
    }

    private Object getBadlionSettings(Object badlionInstance) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // the method called "fu"
        Method methodGetBadlionSettings = badlionInstance.getClass().getDeclaredMethod("fu");
        return methodGetBadlionSettings.invoke(badlionInstance);
    }

    private Object getWrapperInstance() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // get wrapper class
        Class<?> wrapperClass = Class.forName("net.badlion.client.Wrapper");
        // get instance
        Method methodGetInstance = wrapperClass.getDeclaredMethod("getInstance");
        return methodGetInstance.invoke(null);
    }

    private JsonObject getStoreItems(Object instance) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method methodGetStoreItems = instance.getClass().getDeclaredMethod("getStoreItems");
        String jsonString = (String) methodGetStoreItems.invoke(instance);
        // parse json
        return new Gson().fromJson(jsonString, JsonObject.class);
    }
}
