package com.example.pos.adapters;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customerdisplayhandler.model.CustomerDisplay;
import com.example.pos.R;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.List;

public class ConnectedCustomerDisplayAdapter extends RecyclerView.Adapter<ConnectedCustomerDisplayAdapter.ViewHolder> {
    private static final String TAG = ConnectedCustomerDisplayAdapter.class.getSimpleName();
    private final List<CustomerDisplay> connectedCustomerDisplays;
    private final OnItemClickListener itemClickListener;

    // Constructor
    public ConnectedCustomerDisplayAdapter(OnItemClickListener itemClickListener) {
        this.connectedCustomerDisplays = new ArrayList<>();
        this.itemClickListener = itemClickListener;
    }

    public synchronized void updateConnectedDisplayList(List<CustomerDisplay> connectedCustomerDisplays) {
            try {
                this.connectedCustomerDisplays.clear();
                this.connectedCustomerDisplays.addAll(connectedCustomerDisplays);
                Log.d(TAG, "Connected displays updated: " + connectedCustomerDisplays.size());
                notifyDataSetChanged();
            } catch (Exception e) {
                Log.e(TAG, "Error updating search results: " + e.getMessage());
            }
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView customerDisplayName;
        private final TextView customerDisplayIpAddress;
        private final View itemView;
        private final ImageView troubleshootingButton;
        private final ImageView disconnectButton;
        private final MaterialSwitch displayConnectionSwitch;
        private OnItemClickListener itemClickListener;

        public ViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            this.itemClickListener = listener;
            customerDisplayName = itemView.findViewById(R.id.customer_display_name);
            customerDisplayIpAddress = itemView.findViewById(R.id.customer_display_ip_address);
            troubleshootingButton = itemView.findViewById(R.id.customer_display_troubleshoot_button);
            disconnectButton = itemView.findViewById(R.id.customer_display_disconnect_button);
            displayConnectionSwitch = itemView.findViewById(R.id.customer_display_connection_switch);
            this.itemView = itemView;
        }

        public void bind(CustomerDisplay customerDisplay) {
            Log.d(TAG, "Binding customer display: " + customerDisplay.getCustomerDisplayName());
            customerDisplayName.setText(customerDisplay.getCustomerDisplayName());
            customerDisplayIpAddress.setText(customerDisplay.getCustomerDisplayIpAddress());
            displayConnectionSwitch.setChecked(customerDisplay.getIsActivated());
            // Prevent toggling during updates
            displayConnectionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (customerDisplay.getIsActivated() == isChecked) {
                    return;
                }
                if (displayConnectionSwitch.isPressed()) {
                    itemClickListener.onConnectionSwitchToggle(customerDisplay);
                }
            });
            troubleshootingButton.setOnClickListener(v -> itemClickListener.onTroubleshootClick(customerDisplay));
            disconnectButton.setOnClickListener(v -> itemClickListener.onDisconnectClick(customerDisplay));
            itemView.setOnClickListener(v -> itemClickListener.onItemClick(customerDisplay));
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.connected_display_list_item, parent, false);
        return new ViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(connectedCustomerDisplays.get(position));
    }

    @Override
    public int getItemCount() {
        return connectedCustomerDisplays.size();
    }

    // Interface for handling item clicks
    public interface OnItemClickListener {
        void onItemClick(CustomerDisplay customerDisplay);

        void onTroubleshootClick(CustomerDisplay customerDisplay);

        void onDisconnectClick(CustomerDisplay customerDisplay);

        void onConnectionSwitchToggle(CustomerDisplay customerDisplay);
    }
}
