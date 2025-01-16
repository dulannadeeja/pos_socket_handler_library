package com.example.customerdisplayhandler.utils;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public interface IJsonUtil {
    <T>T toObj(String jsonString, Class<T> classOfObj);
    String toJson(Object obj);
    <T> T toTypedObj(String json, TypeToken<T> typeToken);
}
