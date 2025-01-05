package com.example.pos.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.customerdisplayhandler.model.ServiceInfo;
import com.example.pos.R;
import java.util.ArrayList;
import java.util.List;

public class SearchListAdapter extends RecyclerView.Adapter<SearchListAdapter.ViewHolder> {
    private static final String TAG = SearchListAdapter.class.getSimpleName();
    private final List<ServiceInfo> searchResults;
    private final OnItemClickListener itemClickListener;

    // Constructor
    public SearchListAdapter(OnItemClickListener itemClickListener) {
        this.searchResults = new ArrayList<>();
        this.itemClickListener = itemClickListener;
    }

    public void updateSearchResults(List<ServiceInfo> searchResults) {
        try {
            Log.d(TAG, "Updating search results" + searchResults.size());
            this.searchResults.clear();
            this.searchResults.addAll(searchResults);
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

        public void bind(ServiceInfo serviceInfo) {
            Log.d(TAG, "Binding server info: " + serviceInfo.getDeviceName());
            customerDisplayName.setText(serviceInfo.getDeviceName());
            customerDisplayIpAddress.setText(serviceInfo.getIpAddress());
            itemView.setOnClickListener(v -> itemClickListener.onItemClick(serviceInfo));
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_list_item_layout, parent, false);
        return new ViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(searchResults.get(position));
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    // Interface for handling item clicks
    public interface OnItemClickListener {
        void onItemClick(ServiceInfo serviceInfo);
    }
}
