package com.example.customerdisplayhandler.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManagerImpl implements ISharedPrefManager{
    private static final String PREF_NAME = "app_preferences";
    private static SharedPrefManagerImpl instance;
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    // Private constructor to enforce Singleton pattern
    private SharedPrefManagerImpl(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Singleton method to initialize and access the instance
    public static synchronized SharedPrefManagerImpl getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManagerImpl(context.getApplicationContext());
        }
        return instance;
    }

    // Store a String value
    public void putString(String key, String value) {
        editor.putString(key, value).apply();
    }

    // Retrieve a String value
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    // Remove a specific key
    public void remove(String key) {
        editor.remove(key).apply();
    }

    // Clear all stored data
    public void clearAll() {
        editor.clear().apply();
    }
}
