package com.example.pos.ui;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.os.Handler;
import android.os.HandlerThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.customerdisplayhandler.api.ICustomerDisplayManager;
import com.example.customerdisplayhandler.core.interfaces.IConnectedServerManager;
import com.example.customerdisplayhandler.model.CustomerDisplay;
import com.example.customerdisplayhandler.model.ServiceInfo;
import com.example.pos.App;
import com.example.pos.MainActivity;
import com.example.pos.R;


public class PairingFragment extends DialogFragment {

    public static final String TAG = PairingFragment.class.getSimpleName();
    public static final String ARG_SERVICE_INFO = "serviceInfo";
    private ServiceInfo serviceInfo;
    private HandlerThread handlerThread;
    private Handler backgroundHandler;
    private Handler uiHandler;
    private ICustomerDisplayManager customerDisplayManager;
    private TextView pairingStatusTextView;
    private MainActivity mainActivity;
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
        customerDisplayManager = app.getCustomerDisplayManager();

        mainActivity = (MainActivity) requireActivity();

        // Initialize the HandlerThread for background tasks
        handlerThread = new HandlerThread("PairingBackgroundThread");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper());

        // Handler for updating the UI
        uiHandler = new Handler(requireActivity().getMainLooper());

        pairingStatusTextView = view.findViewById(R.id.pairing_status_tv);
        registerPairingServerCallbacks();
    }

    private void registerPairingServerCallbacks(){
        customerDisplayManager.startPairingServer(serviceInfo, new IConnectedServerManager.OnPairingServerListener() {
            @Override
            public void onPairingServerStarted() {
                requireActivity().runOnUiThread(() -> {
                    pairingStatusTextView.setText("Searching for customer display...");
                });
            }

            @Override
            public void onConnectionRequestSent() {
                runWithDelay(2000, () -> {
                    uiHandler.post(() -> {
                        pairingStatusTextView.setText("Connection request sent, waiting for approval...");
                    });
                });
            }

            @Override
            public void onConnectionRequestApproved(ServiceInfo serviceInfo) {
                uiHandler.post(() -> {
                    pairingStatusTextView.setText("Connection approval received from customer display...");
                });
                runWithDelay(2000, () ->{
                    onCustomerDisplayConnected(serviceInfo);
                });
            }

            @Override
            public void onConnectionRequestRejected() {
                uiHandler.post(() -> {
                    pairingStatusTextView.setText("Connection request rejected by customer display...");
                    pairingStatusTextView.setTextColor(requireActivity().getColor(R.color.errorColor));
                });
            }

            @Override
            public void onPairingServerFailed(String message) {
                if(isAdded()){
                    requireActivity().runOnUiThread(() -> {
                        pairingStatusTextView.setText(message);
                        pairingStatusTextView.setTextColor(requireActivity().getColor(R.color.errorColor));
                    });
                }
            }
        });
    }

    private void onCustomerDisplayConnected(ServiceInfo serviceInfo){

        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        AddCustomerDisplayFragment addCustomerDisplayFragment = (AddCustomerDisplayFragment) fragmentManager.findFragmentByTag(AddCustomerDisplayFragment.TAG);
        CustomerDisplaySettingsDialogFragment customerDisplaySettingsDialogFragment = (CustomerDisplaySettingsDialogFragment) fragmentManager.findFragmentByTag(CustomerDisplaySettingsDialogFragment.TAG);

        customerDisplayManager.addConnectedDisplay(serviceInfo.getServerId(), serviceInfo.getDeviceName(), serviceInfo.getIpAddress(), new ICustomerDisplayManager.AddCustomerDisplayListener() {
            @Override
            public void onCustomerDisplayAdded(CustomerDisplay customerDisplay) {
                uiHandler.post(()->{
                    addCustomerDisplayFragment.dismiss();
                    customerDisplaySettingsDialogFragment.refreshConnectedDisplays();
                    dismiss();
                    mainActivity.showToast(customerDisplay.getCustomerDisplayName() + " added successfully.");
                });
            }

            @Override
            public void onCustomerDisplayAddFailed(String errorMessage) {
                uiHandler.post(()->{
                    dismiss();
                    mainActivity.showToast(errorMessage);
                });
            }
        });
    }

    /**
     * Runs a task with a specified delay on the background thread.
     * @param delayMillis The delay in milliseconds.
     * @param task The task to execute after the delay.
     */
    private void runWithDelay(long delayMillis, Runnable task) {
        backgroundHandler.post(() -> {
            try {
                Thread.sleep(delayMillis);
                task.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        customerDisplayManager.stopPairingServer();
    }
}