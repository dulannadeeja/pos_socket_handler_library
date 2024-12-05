package com.example.pos.ui;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.example.customerdisplayhandler.ui.CustomerDisplaySettingsChildFragment;
import com.example.pos.R;

public class CustomerDisplaySettingsDialogFragment extends DialogFragment {

    private static final String TAG = CustomerDisplaySettingsDialogFragment.class.getSimpleName();
    private CustomerDisplaySettingsChildFragment customerDisplaySettingsChildFragment;

    public CustomerDisplaySettingsDialogFragment() {
        // Required empty public constructor
    }

    public static CustomerDisplaySettingsDialogFragment newInstance(CustomerDisplaySettingsChildFragment customerDisplaySettingsChildFragment) {
        CustomerDisplaySettingsDialogFragment fragment = new CustomerDisplaySettingsDialogFragment();
        fragment.customerDisplaySettingsChildFragment = customerDisplaySettingsChildFragment;
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
        return inflater.inflate(R.layout.dialog_fragment_customer_display_settings, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        configureDialogWindow();
    }

    private void configureDialogWindow() {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.white);
        }
    }
}