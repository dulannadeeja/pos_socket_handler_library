package com.example.pos.ui;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.example.customerdisplayhandler.model.CustomerDisplay;
import com.example.pos.R;

import java.lang.ref.WeakReference;

public class SavingDialogFragment extends DialogFragment {
    public static final String TAG = SavingDialogFragment.class.getSimpleName();
    private static final String CUSTOMER_DISPLAY = "customerDisplay";
    private CustomerDisplay customerDisplay;
    private CustomerDisplayViewModel customerDisplayViewModel;
    private CustomerDisplayViewModel.OnUpdateDisplayListener listener;

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

        // Obtain the ViewModel
        customerDisplayViewModel = new ViewModelProvider(requireActivity()).get(CustomerDisplayViewModel.class);

        // Set up the listener with a WeakReference to avoid memory leaks
        WeakReference<SavingDialogFragment> weakReference = new WeakReference<>(this);
        listener = new CustomerDisplayViewModel.OnUpdateDisplayListener() {
            @Override
            public void onDisplayUpdateComplete() {
                SavingDialogFragment fragment = weakReference.get();
                if (fragment != null && fragment.isAdded()) {
                    fragment.dismiss();
                }
            }
        };

        // Pass the listener to the ViewModel
        customerDisplayViewModel.onUpdateCustomerDisplay(customerDisplay, listener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Clear the listener to avoid memory leaks
        if (listener != null) {
            customerDisplayViewModel.removeUpdateDisplayListener();
            listener = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Uncomment to configure dialog window if needed
        // configureDialogWindow();
    }

    private void configureDialogWindow() {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            // Uncomment to set a custom background if needed
            // getDialog().getWindow().setBackgroundDrawableResource(android.R.color.white);
        }
    }
}
