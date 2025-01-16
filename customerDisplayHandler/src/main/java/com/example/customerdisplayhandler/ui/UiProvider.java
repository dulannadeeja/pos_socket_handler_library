package com.example.customerdisplayhandler.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.customerdisplayhandler.R;
import com.example.customerdisplayhandler.ui.callbacks.AddNewDisplayFabListener;

public class UiProvider {
    public static View getCustomerDisplaySettingsView(LayoutInflater inflater, ViewGroup container, AddNewDisplayFabListener addNewDisplayFabListener) {
        View view = inflater.inflate(R.layout.customer_display_settings_layout, container, false);
        view.findViewById(R.id.add_display_fab).setOnClickListener(v -> addNewDisplayFabListener.onClickAddNewDisplayFab());
        return view;
    }
}
