package com.example.pos.adaptors;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pos.R;
import com.example.pos.model.ServerInfo;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.SearchViewHolder> {
    private List<ServerInfo> serverInfoList;
    private OnServerSelected onServerSelected;

    public ListAdapter(List<ServerInfo> serverInfoList, OnServerSelected onServerSelected) {
        this.serverInfoList = serverInfoList;
        this.onServerSelected = onServerSelected;
    }

    public void setServerInfoList(List<ServerInfo> serverInfoList) {
        this.serverInfoList = serverInfoList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_item, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        holder.bind(serverInfoList.get(position));
    }

    @Override
    public int getItemCount() {
        return serverInfoList.size();
    }

    public class SearchViewHolder extends RecyclerView.ViewHolder {
        private TextView tvServerName;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServerName = itemView.findViewById(R.id.tv_search_item);
        }

        public void bind(ServerInfo serverInfo) {
            tvServerName.setText(serverInfo.getServerDeviceName());
            itemView.setOnClickListener(v -> onServerSelected.onServerSelected(serverInfo));
        }
    }

    public interface OnServerSelected {
        void onServerSelected(ServerInfo serverInfo);
    }
}
