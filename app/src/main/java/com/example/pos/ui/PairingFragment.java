package com.example.pos.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.customerdisplayhandler.api.ICustomerDisplayManager;
import com.example.customerdisplayhandler.model.ServiceInfo;
import com.example.pos.App;
import com.example.pos.MainActivity;
import com.example.pos.R;

public class PairingFragment extends DialogFragment {
    public static final String TAG = PairingFragment.class.getSimpleName();
    private static final String ARG_SERVICE_INFO = "serviceInfo";
    private ServiceInfo serviceInfo;
    private TextView pairingStatusTextView;
    private PairingViewModel pairingViewModel;
    private ICustomerDisplayManager customerDisplayManager;

    public static PairingFragment newInstance(ServiceInfo serviceInfo) {
        PairingFragment fragment = new PairingFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SERVICE_INFO, serviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            serviceInfo = (ServiceInfo) getArguments().getSerializable(ARG_SERVICE_INFO);
        }

        // Initialize the ViewModel
        pairingViewModel = new ViewModelProvider(this).get(PairingViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pairing, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pairingStatusTextView = view.findViewById(R.id.pairing_status_tv);

        // Observe pairing status changes
        pairingViewModel.getPairingStatus().observe(getViewLifecycleOwner(), status -> {
            pairingStatusTextView.setText(status);
            // You can add more UI updates based on the status if needed
        });

        // Get the customer display manager
        App app = (App) requireActivity().getApplication();
        customerDisplayManager = app.getCustomerDisplayManager();

        // Start pairing process
        pairingViewModel.setCustomerDisplayManager(customerDisplayManager);
        pairingViewModel.startPairing(serviceInfo, this::onCustomerDisplayConnected);
    }

    private void onCustomerDisplayConnected(ServiceInfo serviceInfo) {
        Log.d(TAG, "Customer display connected: " + serviceInfo.getDeviceName());
        MainActivity mainActivity = (MainActivity) requireActivity();
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        AddCustomerDisplayFragment addCustomerDisplayFragment = (AddCustomerDisplayFragment) fragmentManager.findFragmentByTag(AddCustomerDisplayFragment.TAG);
        CustomerDisplaySettingsDialogFragment customerDisplaySettingsDialogFragment = (CustomerDisplaySettingsDialogFragment) fragmentManager.findFragmentByTag(CustomerDisplaySettingsDialogFragment.TAG);

        if (addCustomerDisplayFragment != null) {
            addCustomerDisplayFragment.dismiss();
        }
        if (customerDisplaySettingsDialogFragment != null) {
            customerDisplaySettingsDialogFragment.refreshConnectedDisplays();
        }
        dismiss();
        mainActivity.showToast(serviceInfo.getDeviceName() + " connected successfully!");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
