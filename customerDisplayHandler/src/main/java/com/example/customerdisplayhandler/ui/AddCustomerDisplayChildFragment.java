package com.example.customerdisplayhandler.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.customerdisplayhandler.R;

public class AddCustomerDisplayChildFragment extends Fragment {

    public AddCustomerDisplayChildFragment() {
        // Required empty public constructor
    }

    public static AddCustomerDisplayChildFragment newInstance(String param1, String param2) {
        AddCustomerDisplayChildFragment fragment = new AddCustomerDisplayChildFragment();
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
        return inflater.inflate(R.layout.fragment_add_customer_display_child, container, false);
    }
}