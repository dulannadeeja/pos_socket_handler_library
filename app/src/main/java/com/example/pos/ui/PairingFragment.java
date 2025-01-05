package com.example.pos.ui;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.customerdisplayhandler.api.ICustomerDisplayManager;
import com.example.customerdisplayhandler.core.interfaces.IConnectedServerManager;
import com.example.customerdisplayhandler.model.ServerInfo;
import com.example.customerdisplayhandler.model.ServiceInfo;
import com.example.pos.App;
import com.example.pos.R;


public class PairingFragment extends DialogFragment {

    public static final String TAG = PairingFragment.class.getSimpleName();
    public static final String ARG_SERVICE_INFO = "serviceInfo";
    private ServiceInfo serviceInfo;
    private ICustomerDisplayManager ICustomerDisplayManager;
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
        ICustomerDisplayManager = app.getCustomerDisplayManager();

        TextView pairingStatusTextView = view.findViewById(R.id.pairing_status_tv);

        ICustomerDisplayManager.startPairingServer(serviceInfo, new IConnectedServerManager.OnPairingServerListener() {
            @Override
            public void onPairingServerStarted() {
                requireActivity().runOnUiThread(() -> {
                    pairingStatusTextView.setText("Searching for customer display...");
                });
            }

            @Override
            public void onConnectionRequestSent() {
                requireActivity().runOnUiThread(() -> {
                    // make some delay to show the message without freezing the UI
                    CountDownTimer countDownTimer = new CountDownTimer(2000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                        }

                        @Override
                        public void onFinish() {
                            pairingStatusTextView.setText("Connection request sent, waiting for approval...");
                        }
                    };
                    countDownTimer.start();
                });
            }

            @Override
            public void onConnectionRequestApproved() {
                requireActivity().runOnUiThread(() -> {
                    pairingStatusTextView.setText("Connection approval received from customer display...");
                    CountDownTimer countDownTimer = new CountDownTimer(2000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                        }

                        @Override
                        public void onFinish() {
                            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                            AddCustomerDisplayFragment addCustomerDisplayFragment = (AddCustomerDisplayFragment) fragmentManager.findFragmentByTag(AddCustomerDisplayFragment.TAG);
                            addCustomerDisplayFragment.dismiss();
                            CustomerDisplaySettingsDialogFragment customerDisplaySettingsDialogFragment = (CustomerDisplaySettingsDialogFragment) fragmentManager.findFragmentByTag(CustomerDisplaySettingsDialogFragment.TAG);
                            customerDisplaySettingsDialogFragment.refreshConnectedCustomerDisplays();
                            dismiss();
                        }
                    };
                    countDownTimer.start();
                });
            }

            @Override
            public void onConnectionRequestRejected() {
                requireActivity().runOnUiThread(() -> {
                pairingStatusTextView.setText("Connection request rejected by customer display...");
                });
            }

            @Override
            public void onPairingServerFailed(String message) {
                requireActivity().runOnUiThread(() -> {
                    pairingStatusTextView.setText(message);
                });
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ICustomerDisplayManager.stopPairingServer();
    }
}