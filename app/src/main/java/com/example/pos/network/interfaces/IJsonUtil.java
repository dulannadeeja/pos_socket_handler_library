package com.example.pos.network.interfaces;

public interface IJsonUtil {
    <T>T toObj(String jsonString, Class<T> classOfObj);
    String toJson(Object obj);
}
