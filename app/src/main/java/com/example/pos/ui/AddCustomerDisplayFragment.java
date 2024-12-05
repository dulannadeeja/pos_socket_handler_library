package com.example.pos.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pos.R;


public class AddCustomerDisplayFragment extends Fragment {

    public AddCustomerDisplayFragment() {
        // Required empty public constructor
    }

    public static AddCustomerDisplayFragment newInstance(String param1, String param2) {
        return new AddCustomerDisplayFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_customer_display, container, false);
    }
}