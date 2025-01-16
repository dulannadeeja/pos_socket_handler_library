package com.example.pos.adapters;

import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customerdisplayhandler.model.CustomerDisplay;
import com.example.pos.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class FailedDisplayListAdapter extends RecyclerView.Adapter<FailedDisplayListAdapter.ViewHolder> {
    private final OnTroubleshootClickListener itemClickListener;
    private final List<CustomerDisplay> failedCustomerDisplays;

    public FailedDisplayListAdapter(List<CustomerDisplay> failedCustomerDisplays, OnTroubleshootClickListener listener) {
        this.failedCustomerDisplays = failedCustomerDisplays;
        this.itemClickListener = listener;
    }

    public void updateFailedCustomerDisplays(List<CustomerDisplay> failedCustomerDisplays) {
        this.failedCustomerDisplays.clear();
        this.failedCustomerDisplays.addAll(failedCustomerDisplays);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView customerDisplayName;
        private final TextView customerDisplayIpAddress;
        private final MaterialButton troubleshootButton;
        private final OnTroubleshootClickListener itemClickListener;

        public ViewHolder(View itemView, OnTroubleshootClickListener listener) {
            super(itemView);
            this.itemClickListener = listener;
            customerDisplayName = itemView.findViewById(R.id.customer_display_name);
            customerDisplayIpAddress = itemView.findViewById(R.id.customer_display_ip_address);
            troubleshootButton = itemView.findViewById(R.id.customer_display_troubleshoot_button);
        }

        public void bind(CustomerDisplay customerDisplay) {
            customerDisplayName.setText(customerDisplay.getCustomerDisplayName());
            customerDisplayIpAddress.setText(customerDisplay.getCustomerDisplayIpAddress());
            troubleshootButton.setOnClickListener(v -> itemClickListener.onTroubleshootClick(customerDisplay));
        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.failed_customer_display_item, parent, false);
        return new ViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(failedCustomerDisplays.get(position));
    }

    @Override
    public int getItemCount() {
        return failedCustomerDisplays.size();
    }

    public interface OnTroubleshootClickListener {
        void onTroubleshootClick(CustomerDisplay customerDisplay);
    }
}

