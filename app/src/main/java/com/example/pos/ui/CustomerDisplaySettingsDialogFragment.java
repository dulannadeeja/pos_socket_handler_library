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
import androidx.lifecycle.ViewModelProvider;
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
    private CustomerDisplayViewModel customerDisplayViewModel;
    private AddNewDisplayFabListener addNewDisplayFabListener;
    private RecyclerView connectedDisplaysRecyclerView;
    private LinearLayout noConnectedDisplaysLayout;
    private ConnectedCustomerDisplayAdapter connectedDisplayAdapter;

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
        initViewModels();
        initRecyclerView();
        observeViewModels();
    }

    private void initViewModels() {
        customerDisplayViewModel = new ViewModelProvider(requireActivity()).get(CustomerDisplayViewModel.class);
        customerDisplayViewModel.fetchConnectedDisplays();
    }

    private void observeViewModels() {
        customerDisplayViewModel.getPairedDisplays().observe(getViewLifecycleOwner(), this::updateDisplayVisibility);
    }

    private void initializeViews(@NonNull View view) {
        connectedDisplaysRecyclerView = view.findViewById(com.example.customerdisplayhandler.R.id.connected_displays_recycler_view);
        noConnectedDisplaysLayout = view.findViewById(com.example.customerdisplayhandler.R.id.no_connected_displays_layout);
        connectedDisplaysRecyclerView.setVisibility(View.GONE);
        noConnectedDisplaysLayout.setVisibility(View.GONE);
    }

    private void showEditCustomerDisplayFragment(CustomerDisplay customerDisplay) {
        EditCustomerDisplayFragment editCustomerDisplayFragment = EditCustomerDisplayFragment.newInstance(customerDisplay);
        editCustomerDisplayFragment.show(requireActivity().getSupportFragmentManager(), EditCustomerDisplayFragment.TAG);
    }

    private void initRecyclerView() {
        connectedDisplayAdapter = new ConnectedCustomerDisplayAdapter(getItemClickListener());
        connectedDisplaysRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        connectedDisplaysRecyclerView.setAdapter(connectedDisplayAdapter);
    }

    private ConnectedCustomerDisplayAdapter.OnItemClickListener getItemClickListener() {
        return new ConnectedCustomerDisplayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CustomerDisplay customerDisplay) {
                Log.d(TAG, "Customer display clicked: " + customerDisplay.getCustomerDisplayName());
                showEditCustomerDisplayFragment(customerDisplay);
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
                customerDisplayViewModel.disconnectCustomerDisplay(customerDisplay);
            }

            @Override
            public void onConnectionSwitchToggle(CustomerDisplay customerDisplay) {
                customerDisplayViewModel.toggleCustomerDisplayActivation(customerDisplay, () -> {
                    // do nothing on failure, everything is handled by the ViewModel
                });
            }
        };
    }

    private void updateDisplayVisibility(List<CustomerDisplay> pairedDisplays) {
        Log.d(TAG, "Paired displays: " + pairedDisplays.size());
        if (pairedDisplays.isEmpty()) {
            connectedDisplaysRecyclerView.setVisibility(View.GONE);
            noConnectedDisplaysLayout.setVisibility(View.VISIBLE);
        } else {
            connectedDisplayAdapter.updateConnectedDisplayList(pairedDisplays);
            connectedDisplaysRecyclerView.setVisibility(View.VISIBLE);
            noConnectedDisplaysLayout.setVisibility(View.GONE);
        }
    }

    private void updateLoadingState(Boolean isLoading) {
        if (isLoading) {
            connectedDisplaysRecyclerView.setVisibility(View.GONE);
            noConnectedDisplaysLayout.setVisibility(View.GONE);
        }
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
