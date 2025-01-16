package com.example.customerdisplayhandler.model;

import java.util.List;

public class ComboGroup {
    private final String group;
    private final List<String> items;

    public ComboGroup(String group,List<String> items){
        this.group = group;
        this.items = items;
    }

    public String getGroup() {
        return group;
    }

    public List<String> getItems() {
        return items;
    }
}
