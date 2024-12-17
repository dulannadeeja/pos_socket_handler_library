package com.example.customerdisplayhandler.utils;

public interface IJsonUtil {
    <T>T toObj(String jsonString, Class<T> classOfObj);
    String toJson(Object obj);
}
