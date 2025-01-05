package com.example.pos.ui;

import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.example.customerdisplayhandler.api.ICustomerDisplayManager;
import com.example.customerdisplayhandler.model.ServerInfo;
import com.example.customerdisplayhandler.ui.UiProvider;
import com.example.customerdisplayhandler.ui.callbacks.AddNewDisplayFabListener;
import com.example.pos.App;
import com.example.pos.R;
import com.example.pos.adapters.ConnectedCustomerDisplayAdapter;

import java.util.ArrayList;
import java.util.List;

public class CustomerDisplaySettingsDialogFragment extends DialogFragment {

    public static final String TAG = CustomerDisplaySettingsDialogFragment.class.getSimpleName();
    private AddNewDisplayFabListener addNewDisplayFabListener;
    private RecyclerView connectedCustomerDisplaysRecyclerView;
    private LinearLayout noConnectedCustomerDisplaysLayout;
    private ConnectedCustomerDisplayAdapter connectedCustomerDisplayAdapter;
    private ICustomerDisplayManager ICustomerDisplayManager;

    public CustomerDisplaySettingsDialogFragment() {
        // Required empty public constructor
    }

    public static CustomerDisplaySettingsDialogFragment newInstance() {
        return new CustomerDisplaySettingsDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        addNewDisplayFabListener = this::showAddCustomerDisplayFragment;

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.dialog_fragment_customer_display_settings, container, false);
        View childView = UiProvider.getCustomerDisplaySettingsView(inflater, container,addNewDisplayFabListener);
        ViewGroup childContainer = rootView.findViewById(R.id.main);
        childContainer.addView(childView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        App app = (App) requireActivity().getApplication();
        ICustomerDisplayManager = app.getCustomerDisplayManager();

        connectedCustomerDisplaysRecyclerView = view.findViewById(com.example.customerdisplayhandler.R.id.connected_displays_recycler_view);
        noConnectedCustomerDisplaysLayout = view.findViewById(com.example.customerdisplayhandler.R.id.no_connected_displays_layout);

        connectedCustomerDisplayAdapter = new ConnectedCustomerDisplayAdapter(serverInfo -> {
            Log.d(TAG, "Server info clicked: " + serverInfo.getServerDeviceName());
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        connectedCustomerDisplaysRecyclerView.setLayoutManager(linearLayoutManager);
        connectedCustomerDisplaysRecyclerView.setAdapter(connectedCustomerDisplayAdapter);

        // TODO: Get paired customer displays
        List<ServerInfo> pairedCustomerDisplays = new ArrayList<>();
        if (pairedCustomerDisplays.isEmpty()) {
            connectedCustomerDisplaysRecyclerView.setVisibility(View.GONE);
            noConnectedCustomerDisplaysLayout.setVisibility(View.VISIBLE);
        } else {
            connectedCustomerDisplayAdapter.updateConnectedDisplayList(pairedCustomerDisplays);
            connectedCustomerDisplaysRecyclerView.setVisibility(View.VISIBLE);
            noConnectedCustomerDisplaysLayout.setVisibility(View.GONE);
        }

    }

    public void refreshConnectedCustomerDisplays() {
        // TODO: Implement this method
        List<ServerInfo> pairedCustomerDisplays = new ArrayList<>();
        if (pairedCustomerDisplays.isEmpty()) {
            connectedCustomerDisplaysRecyclerView.setVisibility(View.GONE);
            noConnectedCustomerDisplaysLayout.setVisibility(View.VISIBLE);
        } else {
            connectedCustomerDisplayAdapter.updateConnectedDisplayList(pairedCustomerDisplays);
            connectedCustomerDisplaysRecyclerView.setVisibility(View.VISIBLE);
            noConnectedCustomerDisplaysLayout.setVisibility(View.GONE);
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