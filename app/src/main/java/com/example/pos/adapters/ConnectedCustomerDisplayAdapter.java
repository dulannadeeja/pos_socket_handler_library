package com.example.pos.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customerdisplayhandler.model.ServerInfo;
import com.example.pos.R;

import java.util.ArrayList;
import java.util.List;

public class ConnectedCustomerDisplayAdapter extends RecyclerView.Adapter<ConnectedCustomerDisplayAdapter.ViewHolder> {
    private static final String TAG = ConnectedCustomerDisplayAdapter.class.getSimpleName();
    private final List<ServerInfo> connectedCustomerDisplays;
    private final OnItemClickListener itemClickListener;

    // Constructor
    public ConnectedCustomerDisplayAdapter(OnItemClickListener itemClickListener) {
        this.connectedCustomerDisplays = new ArrayList<>();
        this.itemClickListener = itemClickListener;
    }

    public void updateConnectedDisplayList(List<ServerInfo> connectedCustomerDisplays) {
        try {
            this.connectedCustomerDisplays.clear();
            this.connectedCustomerDisplays.addAll(connectedCustomerDisplays);
            notifyDataSetChanged();
        } catch (Exception e) {
            Log.e(TAG, "Error updating search results: " + e.getMessage());
        }
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView customerDisplayName;
        private final TextView customerDisplayIpAddress;
        private final View itemView;;
        private OnItemClickListener itemClickListener;

        public ViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            this.itemClickListener = listener;
            customerDisplayName = itemView.findViewById(R.id.customer_display_name);
            customerDisplayIpAddress = itemView.findViewById(R.id.customer_display_ip_address);
            this.itemView = itemView;
        }

        public void bind(ServerInfo serverInfo) {
            Log.d(TAG, "Binding server info: " + serverInfo.getServerDeviceName());
            customerDisplayName.setText(serverInfo.getServerDeviceName());
            customerDisplayIpAddress.setText(serverInfo.getServerIpAddress());
            itemView.setOnClickListener(v -> itemClickListener.onItemClick(serverInfo));
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
        void onItemClick(ServerInfo serverInfo);
    }
}
