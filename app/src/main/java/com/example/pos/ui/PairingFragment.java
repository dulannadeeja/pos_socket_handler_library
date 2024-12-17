package com.example.pos.ui;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.customerdisplayhandler.api.CustomerDisplayManager;
import com.example.customerdisplayhandler.core.interfaces.IConnectedServerManager;
import com.example.customerdisplayhandler.model.ServerInfo;
import com.example.pos.App;
import com.example.pos.R;


public class PairingFragment extends DialogFragment {

    public static final String TAG = PairingFragment.class.getSimpleName();
    public static final String ARG_SERVER_INFO = "serverInfo";
    private ServerInfo serverInfo;
    private CustomerDisplayManager customerDisplayManager;
    public static PairingFragment newInstance(ServerInfo serverInfo) {
        PairingFragment fragment = new PairingFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SERVER_INFO, serverInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            serverInfo = (ServerInfo) getArguments().getSerializable(ARG_SERVER_INFO);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pairing, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        App app = (App) requireActivity().getApplication();
        customerDisplayManager = app.getCustomerDisplayManager();

        TextView pairingStatusTextView = view.findViewById(R.id.pairing_status_tv);

        customerDisplayManager.startPairingServer(serverInfo, new IConnectedServerManager.OnPairingServerListener() {
            @Override
            public void onPairingServerStarted() {
                requireActivity().runOnUiThread(() -> {
                    pairingStatusTextView.setText("Searching for customer display...");
                });
            }

            @Override
            public void onConnectionRequestSent() {
                requireActivity().runOnUiThread(() -> {
                pairingStatusTextView.setText("Connection request sent...");
                });
            }

            @Override
            public void onConnectionRequestFailed() {
                requireActivity().runOnUiThread(() -> {
                    pairingStatusTextView.setText("Connection request failed...");
                });
            }

            @Override
            public void onConnectionRequestApproved() {
                requireActivity().runOnUiThread(() -> {
                    pairingStatusTextView.setText("Connection request approved...");
                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    AddCustomerDisplayFragment addCustomerDisplayFragment = (AddCustomerDisplayFragment) fragmentManager.findFragmentByTag(AddCustomerDisplayFragment.TAG);
                    addCustomerDisplayFragment.dismiss();
                    CustomerDisplaySettingsDialogFragment customerDisplaySettingsDialogFragment = (CustomerDisplaySettingsDialogFragment) fragmentManager.findFragmentByTag(CustomerDisplaySettingsDialogFragment.TAG);
                    customerDisplaySettingsDialogFragment.refreshConnectedCustomerDisplays();
                    dismiss();
                });
            }

            @Override
            public void onConnectionRequestRejected() {
                requireActivity().runOnUiThread(() -> {
                pairingStatusTextView.setText("Connection request rejected...");
                });
            }
        });
    }
}