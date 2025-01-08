package com.example.pos.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customerdisplayhandler.api.ICustomerDisplayManager;
import com.example.customerdisplayhandler.model.CustomerDisplay;
import com.example.customerdisplayhandler.ui.UiProvider;
import com.example.customerdisplayhandler.ui.callbacks.AddNewDisplayFabListener;
import com.example.pos.App;
import com.example.pos.MainActivity;
import com.example.pos.R;
import com.example.pos.adapters.ConnectedCustomerDisplayAdapter;

import java.util.ArrayList;
import java.util.List;

public class CustomerDisplaySettingsDialogFragment extends DialogFragment {

    public static final String TAG = CustomerDisplaySettingsDialogFragment.class.getSimpleName();

    private AddNewDisplayFabListener addNewDisplayFabListener;
    private RecyclerView connectedDisplaysRecyclerView;
    private LinearLayout noConnectedDisplaysLayout;
    private ConnectedCustomerDisplayAdapter connectedDisplayAdapter;
    private ICustomerDisplayManager customerDisplayManager;
    private final List<CustomerDisplay> pairedDisplays = new ArrayList<>();

    public static CustomerDisplaySettingsDialogFragment newInstance() {
        return new CustomerDisplaySettingsDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        addNewDisplayFabListener = this::showAddCustomerDisplayFragment;
        View rootView = inflater.inflate(R.layout.dialog_fragment_customer_display_settings, container, false);
        View childView = UiProvider.getCustomerDisplaySettingsView(inflater, container, addNewDisplayFabListener);
        ViewGroup childContainer = rootView.findViewById(R.id.main);
        childContainer.addView(childView);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupRecyclerView();
        fetchConnectedDisplays();
    }

    private void initializeViews(@NonNull View view) {
        App app = (App) requireActivity().getApplication();
        customerDisplayManager = app.getCustomerDisplayManager();

        connectedDisplaysRecyclerView = view.findViewById(com.example.customerdisplayhandler.R.id.connected_displays_recycler_view);
        noConnectedDisplaysLayout = view.findViewById(com.example.customerdisplayhandler.R.id.no_connected_displays_layout);

        connectedDisplayAdapter = new ConnectedCustomerDisplayAdapter(getItemClickListener());
    }

    private ConnectedCustomerDisplayAdapter.OnItemClickListener getItemClickListener(){
        return new ConnectedCustomerDisplayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CustomerDisplay customerDisplay) {
                Log.d(TAG, "Customer display clicked: " + customerDisplay.getCustomerDisplayName());
            }

            @Override
            public void onTroubleshootClick(CustomerDisplay customerDisplay) {
                requireActivity().runOnUiThread(() -> {
                    TroubleshootDisplayFragment troubleshootDisplayFragment = TroubleshootDisplayFragment.newInstance(customerDisplay);
                    troubleshootDisplayFragment.show(requireActivity().getSupportFragmentManager(), TroubleshootDisplayFragment.TAG);
                });
            }

            @Override
            public void onDisconnectClick(CustomerDisplay customerDisplay) {
                disconnectCustomerDisplay(customerDisplay);
            }

            @Override
            public void onConnectionSwitchToggle(CustomerDisplay customerDisplay) {
                toggleCustomerDisplayActivation(customerDisplay);
            }
        };
    }

    private void disconnectCustomerDisplay(CustomerDisplay customerDisplay){
        customerDisplayManager.removeConnectedDisplay(customerDisplay.getCustomerDisplayID(), new ICustomerDisplayManager.RemoveCustomerDisplayListener() {
            @Override
            public void onCustomerDisplayRemoved() {
                showToast(customerDisplay.getCustomerDisplayName() + " removed successfully");
                refreshConnectedDisplays();
            }

            @Override
            public void onCustomerDisplayRemoveFailed(String errorMessage) {
                Log.e(TAG, "Error removing customer display: " + errorMessage);
                showToast("Error occurred while removing " + customerDisplay.getCustomerDisplayName());
            }
        });
    }

    private void toggleCustomerDisplayActivation(CustomerDisplay customerDisplay){
        customerDisplayManager.toggleCustomerDisplayActivation(customerDisplay.getCustomerDisplayID(), new ICustomerDisplayManager.OnCustomerDisplayActivationToggleListener() {
            @Override
            public void onCustomerDisplayActivated() {
                showToast(customerDisplay.getCustomerDisplayName() + " will now receive updates");
                refreshConnectedDisplays();
            }

            @Override
            public void onCustomerDisplayDeactivated() {
                showToast(customerDisplay.getCustomerDisplayName() + " will no longer receive updates");
                refreshConnectedDisplays();
            }

            @Override
            public void onCustomerDisplayActivationToggleFailed(String errorMessage) {
                Log.e(TAG, "Error toggling customer display activation: " + errorMessage);
                showToast(errorMessage);
            }
        });
    }

    private void setupRecyclerView() {
        connectedDisplaysRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        connectedDisplaysRecyclerView.setAdapter(connectedDisplayAdapter);
        updateDisplayVisibility();
    }

    private void fetchConnectedDisplays() {
        customerDisplayManager.getConnectedDisplays(new ICustomerDisplayManager.GetConnectedDisplaysListener() {
            @Override
            public void onConnectedDisplaysReceived(List<CustomerDisplay> connectedDisplays) {
                Log.d(TAG, "Connected displays received: " + connectedDisplays.size());
                pairedDisplays.clear();
                pairedDisplays.addAll(connectedDisplays);
                updateDisplayVisibility();
            }

            @Override
            public void onConnectedDisplaysReceiveFailed(String errorMessage) {
                Log.e(TAG, "Error getting connected displays: " + errorMessage);
                showToast("Error occurred while getting connected displays");
            }
        });
    }

    private void updateDisplayVisibility() {
        if (pairedDisplays.isEmpty() ) {
            connectedDisplaysRecyclerView.setVisibility(View.GONE);
            noConnectedDisplaysLayout.setVisibility(View.VISIBLE);
        } else {
            connectedDisplayAdapter.updateConnectedDisplayList(pairedDisplays);
            connectedDisplaysRecyclerView.setVisibility(View.VISIBLE);
            noConnectedDisplaysLayout.setVisibility(View.GONE);
        }
    }

    public void refreshConnectedDisplays() {
        fetchConnectedDisplays();
    }

    private void showToast(String message) {
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.showToast(message);
    }

    @Override
    public void onStart() {
        super.onStart();
        configureDialogWindow();
    }

    private void showAddCustomerDisplayFragment() {
        AddCustomerDisplayFragment addCustomerDisplayFragment = AddCustomerDisplayFragment.newInstance();
        addCustomerDisplayFragment.show(requireActivity().getSupportFragmentManager(), AddCustomerDisplayFragment.TAG);
    }

    private void configureDialogWindow() {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.white);
        }
    }
}
