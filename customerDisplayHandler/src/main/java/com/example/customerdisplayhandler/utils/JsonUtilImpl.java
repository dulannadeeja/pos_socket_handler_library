package com.example.customerdisplayhandler.utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class JsonUtilImpl implements IJsonUtil {
    private Gson gson;

    public JsonUtilImpl() {
        gson = new Gson();
    }

    @Override
    public <T> T toObj(String jsonString, Class<T> classOfObj) {
        return gson.fromJson(jsonString, classOfObj);
    }

    @Override
    public String toJson(Object obj) {
        return gson.toJson(obj);
    }

    @Override
    public <T> T toTypedObj(String json, TypeToken<T> typeToken) {
        if (json == null || typeToken == null) {
            throw new IllegalArgumentException("JSON string or TypeToken cannot be null");
        }
        Type type = typeToken.getType();
        return gson.fromJson(json, type);
    }
}
