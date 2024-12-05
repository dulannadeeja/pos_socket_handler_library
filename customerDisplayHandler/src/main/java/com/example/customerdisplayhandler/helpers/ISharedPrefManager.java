package com.example.customerdisplayhandler.helpers;

public interface ISharedPrefManager {
    void putString(String key, String value);
    String getString(String key, String defaultValue);
    public void remove(String key);
    public void clearAll();
}
