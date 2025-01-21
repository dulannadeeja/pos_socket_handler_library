package com.example.pos.ui;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.example.customerdisplayhandler.model.CustomerDisplay;
import com.example.pos.R;


public class SavingDialogFragment extends DialogFragment {
    public static final String TAG = SavingDialogFragment.class.getSimpleName();
    private static final String CUSTOMER_DISPLAY = "customerDisplay";
    private CustomerDisplay customerDisplay;
    private CustomerDisplayViewModel customerDisplayViewModel;

    public static SavingDialogFragment newInstance(CustomerDisplay customerDisplay) {
        SavingDialogFragment fragment = new SavingDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(CUSTOMER_DISPLAY, customerDisplay);
        fragment.setArguments(args);
        return fragment;
    }

    public SavingDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        if (getArguments() != null) {
            customerDisplay = (CustomerDisplay) getArguments().getSerializable(CUSTOMER_DISPLAY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_saving_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        customerDisplayViewModel = new ViewModelProvider(requireActivity()).get(CustomerDisplayViewModel.class);
        customerDisplayViewModel.onUpdateCustomerDisplay(customerDisplay, new CustomerDisplayViewModel.OnUpdateDisplayListener() {
            @Override
            public void onDisplayUpdateComplete() {
                if (isAdded()) {
                    dismiss();
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
//        configureDialogWindow();
    }

    private void configureDialogWindow() {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
//            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.white);
        }
    }
}