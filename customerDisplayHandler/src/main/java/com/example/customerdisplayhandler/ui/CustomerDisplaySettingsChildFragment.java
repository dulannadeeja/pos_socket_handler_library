package com.example.customerdisplayhandler.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.customerdisplayhandler.R;

public class CustomerDisplaySettingsChildFragment extends Fragment {

    private static final String TAG = "CustomerDisplaySettingsChildFragment";
    private CustomerDisplaySettingsChildFragment INSTANCE;

    public CustomerDisplaySettingsChildFragment() {
        // Required empty public constructor
    }

    public static CustomerDisplaySettingsChildFragment newInstance() {
        CustomerDisplaySettingsChildFragment fragment = new CustomerDisplaySettingsChildFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_customer_display_settings_child, container, false);
    }
}