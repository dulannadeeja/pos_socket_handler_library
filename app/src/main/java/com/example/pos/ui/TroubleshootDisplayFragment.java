package com.example.pos.ui;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.customerdisplayhandler.api.ICustomerDisplayManager;
import com.example.customerdisplayhandler.model.CustomerDisplay;
import com.example.customerdisplayhandler.shared.OnTroubleshootListener;
import com.example.pos.App;
import com.example.pos.R;

import java.util.List;


public class TroubleshootDisplayFragment extends DialogFragment {
    public static final String TAG = TroubleshootDisplayFragment.class.getSimpleName();
    private static final String ARG_CUSTOMER_DISPLAY = "customerDisplay";
    private CustomerDisplay customerDisplay;
    private ICustomerDisplayManager customerDisplayManager;
    private TextView troubleshootingStatusTv;
    private TroubleshootViewModel troubleshootViewModel;
    private CustomerDisplayViewModel customerDisplayViewModel;

    public static TroubleshootDisplayFragment newInstance(CustomerDisplay customerDisplay) {
        TroubleshootDisplayFragment fragment = new TroubleshootDisplayFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CUSTOMER_DISPLAY, customerDisplay);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            customerDisplay = (CustomerDisplay) getArguments().getSerializable(ARG_CUSTOMER_DISPLAY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_troubleshoot_display, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        App app = (App) requireActivity().getApplication();
        customerDisplayManager = app.getCustomerDisplayManager();

        // Initialize the ViewModel
        customerDisplayViewModel = new ViewModelProvider(requireActivity()).get(CustomerDisplayViewModel.class);
        troubleshootViewModel = new ViewModelProvider(this).get(TroubleshootViewModel.class);
        troubleshootViewModel.setCustomerDisplayManager(customerDisplayManager);
        troubleshootViewModel.startTroubleshooting(customerDisplay, () -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            CustomerDisplaySettingsDialogFragment customerDisplaySettingsDialogFragment = (CustomerDisplaySettingsDialogFragment) fragmentManager.findFragmentByTag(CustomerDisplaySettingsDialogFragment.TAG);
            if (customerDisplaySettingsDialogFragment != null) {
                customerDisplayViewModel.refreshConnectedDisplays();
            }
            FailedCustomerDisplaysFragment failedCustomerDisplaysFragment = (FailedCustomerDisplaysFragment) fragmentManager.findFragmentByTag(FailedCustomerDisplaysFragment.TAG);
            if (failedCustomerDisplaysFragment != null) {
                failedCustomerDisplaysFragment.removeFailedDisplay(customerDisplay);
            }
        });

        troubleshootingStatusTv = view.findViewById(R.id.troubleshooting_status_tv);

        // Observe troubleshooting status changes
        troubleshootViewModel.getTroubleshootingStatus().observe(getViewLifecycleOwner(), status -> {
            troubleshootingStatusTv.setText(status);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        troubleshootViewModel.stopTroubleshooting();
    }
}