package com.example.customerdisplayhandler.utils;
import com.google.gson.Gson;

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
}
