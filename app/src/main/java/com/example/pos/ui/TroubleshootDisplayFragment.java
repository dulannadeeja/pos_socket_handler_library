package com.example.pos.ui;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

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
    private HandlerThread handlerThread;
    private Handler backgroundHandler;
    private CustomerDisplay customerDisplay;
    private ICustomerDisplayManager customerDisplayManager;
    private TextView troubleshootingStatusTv;

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

        // Initialize the HandlerThread for background tasks
        handlerThread = new HandlerThread("TroubleshootBackgroundThread");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper());

        App app = (App) requireActivity().getApplication();
        customerDisplayManager = app.getCustomerDisplayManager();

        troubleshootingStatusTv = view.findViewById(R.id.troubleshooting_status_tv);

        customerDisplayManager.startManualTroubleshooting(customerDisplay, new OnTroubleshootListener() {
            @Override
            public void onScanningForCustomerDisplays() {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        troubleshootingStatusTv.setText("Looking for customer displays live on the network, please wait it may take up to 30 seconds...");
                        troubleshootingStatusTv.setTextColor(requireActivity().getColor(R.color.textSecondaryColor));
                    });
                }
            }

            @Override
            public void onCustomerDisplayFound() {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        troubleshootingStatusTv.setText("Customer display found. Attempting to connect...");
                        troubleshootingStatusTv.setTextColor(requireActivity().getColor(R.color.textSecondaryColor));
                    });
                }
            }

            @Override
            public void onAttemptingToConnect() {
                runWithDelay(2000, () -> {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            troubleshootingStatusTv.setText("Attempting to connect to customer display...");
                            troubleshootingStatusTv.setTextColor(requireActivity().getColor(R.color.textSecondaryColor));
                        });
                    }
                });
            }

            @Override
            public void onSavingCustomerDisplay() {
                runWithDelay(2000, () -> {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            troubleshootingStatusTv.setText("Saving updated connection info for customer display...");
                            troubleshootingStatusTv.setTextColor(requireActivity().getColor(R.color.textSecondaryColor));
                        });
                    }
                });
            }

            @Override
            public void onTroubleshootCompleted() {
                runWithDelay(2000, () -> {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            troubleshootingStatusTv.setText("Troubleshooting completed successfully.");
                            troubleshootingStatusTv.setTextColor(requireActivity().getColor(R.color.primaryColor));
                            CustomerDisplaySettingsDialogFragment customerDisplaySettingsDialogFragment = (CustomerDisplaySettingsDialogFragment) requireActivity().getSupportFragmentManager().findFragmentByTag(CustomerDisplaySettingsDialogFragment.TAG);
                            if (customerDisplaySettingsDialogFragment != null) {
                                customerDisplaySettingsDialogFragment.refreshConnectedDisplays();
                            }
                        });
                    }
                });
            }

            @Override
            public void onTroubleshootFailed(String message) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        troubleshootingStatusTv.setText(message);
                        troubleshootingStatusTv.setTextColor(requireActivity().getColor(R.color.errorColor));
                    });
                }
            }
        });
    }

    /**
     * Runs a task with a specified delay on the background thread.
     *
     * @param delayMillis The delay in milliseconds.
     * @param task        The task to execute after the delay.
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
        handlerThread.quit();
        customerDisplayManager.disposeCustomerDisplayManager();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handlerThread.quit();
        customerDisplayManager.disposeCustomerDisplayManager();
    }
}