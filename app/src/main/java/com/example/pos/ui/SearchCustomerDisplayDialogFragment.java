package com.example.pos.ui;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.customerdisplayhandler.api.ICustomerDisplayManager;
import com.example.customerdisplayhandler.api.CustomerDisplayManagerImpl;
import com.example.customerdisplayhandler.core.interfaces.INetworkServiceDiscoveryManager;
import com.example.customerdisplayhandler.model.ServiceInfo;
import com.example.pos.App;
import com.example.pos.R;
import com.example.pos.adapters.SearchListAdapter;

import java.util.ArrayList;
import java.util.List;


public class SearchCustomerDisplayDialogFragment extends DialogFragment {
    public static final String TAG = SearchCustomerDisplayDialogFragment.class.getSimpleName();
    private ICustomerDisplayManager customerDisplayManager;
    private final List<ServiceInfo> searchResults = new ArrayList<>();

    public SearchCustomerDisplayDialogFragment() {
        // Required empty public constructor
    }

    public static SearchCustomerDisplayDialogFragment newInstance() {
        return new SearchCustomerDisplayDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App app = (App) requireActivity().getApplication();
        customerDisplayManager = app.getCustomerDisplayManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_customer_display, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ProgressBar searchProgressBar = view.findViewById(R.id.search_progress_bar);
        searchProgressBar.setVisibility(View.INVISIBLE);
        RecyclerView searchResultsRecyclerView = view.findViewById(R.id.customer_display_list);
        SearchListAdapter searchListAdapter = new SearchListAdapter(serverInfo -> {
            // Find the fragment by its tag
            Fragment targetFragment = requireActivity().getSupportFragmentManager()
                    .findFragmentByTag(AddCustomerDisplayFragment.TAG);

            Log.d(TAG, "Selected customer display: " + serverInfo.getDeviceName());

            if (targetFragment instanceof AddCustomerDisplayFragment) {
                Log.d(TAG, "Updating selected customer display");
                ((AddCustomerDisplayFragment) targetFragment).updateSelectedCustomerDisplay(serverInfo);
            } else {
                Log.d(TAG, "Target fragment is not an instance of AddCustomerDisplayFragment");
            }
            dismiss();
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        searchResultsRecyclerView.setLayoutManager(layoutManager);
        searchResultsRecyclerView.setAdapter(searchListAdapter);

        App app = (App) requireActivity().getApplication();
        customerDisplayManager = app.getCustomerDisplayManager();
        customerDisplayManager.startSearchForCustomerDisplays(new INetworkServiceDiscoveryManager.SearchListener() {
            @Override
            public void onSearchStarted() {
                requireActivity().runOnUiThread(() -> {
                    searchProgressBar.setVisibility(View.VISIBLE);
                    searchResults.clear();
                    searchListAdapter.updateSearchResults(searchResults);
                });
            }

            @Override
            public void onSearchCompleted() {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        searchProgressBar.setVisibility(View.INVISIBLE);
                    });
                }
            }

            @Override
            public void onSearchFailed(String errorMessage) {
                requireActivity().runOnUiThread(() -> {
                    searchProgressBar.setVisibility(View.INVISIBLE);
                });
            }

            @Override
            public void onServiceFound(ServiceInfo serviceInfo) {
                requireActivity().runOnUiThread(() -> {
                    if(isAdded()){
                        // Check if the service is already in the list
                        boolean isAlreadyInList = searchResults.stream().anyMatch(existingServiceInfo -> existingServiceInfo.getServerId().equals(serviceInfo.getServerId()));
                        if (!isAlreadyInList) {
                            searchResults.add(serviceInfo);
                            searchListAdapter.updateSearchResults(searchResults);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        customerDisplayManager.stopSearchForCustomerDisplays();
    }
}