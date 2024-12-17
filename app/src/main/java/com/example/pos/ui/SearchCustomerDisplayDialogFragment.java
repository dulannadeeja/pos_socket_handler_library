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

import com.example.customerdisplayhandler.api.CustomerDisplayManager;
import com.example.customerdisplayhandler.api.CustomerDisplayManagerImpl;
import com.example.customerdisplayhandler.model.ServerInfo;
import com.example.customerdisplayhandler.utils.SocketConfigConstants;
import com.example.pos.App;
import com.example.pos.R;
import com.example.pos.adapters.SearchListAdapter;

import java.util.List;


public class SearchCustomerDisplayDialogFragment extends DialogFragment {
    public static final String TAG = SearchCustomerDisplayDialogFragment.class.getSimpleName();
    private CustomerDisplayManager customerDisplayManager;

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

            Log.d(TAG, "Selected customer display: " + serverInfo.getServerID());

            if (targetFragment instanceof AddCustomerDisplayFragment) {
                Log.d(TAG, "Updating selected customer display");
                ((AddCustomerDisplayFragment) targetFragment).updateSelectedCustomerDisplay(serverInfo);
            }else{
                Log.d(TAG, "Target fragment is not an instance of AddCustomerDisplayFragment");
            }
            dismiss();
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        searchResultsRecyclerView.setLayoutManager(layoutManager);
        searchResultsRecyclerView.setAdapter(searchListAdapter);

        customerDisplayManager = CustomerDisplayManagerImpl.newInstance(getContext(), SocketConfigConstants.DEFAULT_SERVER_PORT);
        customerDisplayManager.startSearchForCustomerDisplays(new CustomerDisplayManager.SearchListener() {
            @Override
            public void onSearchStarted() {
                searchProgressBar.setVisibility(View.VISIBLE);
                List<ServerInfo> searchResults = customerDisplayManager.getAvailableCustomerDisplays();
                searchListAdapter.updateSearchResults(searchResults);
            }

            @Override
            public void onSearchCompleted() {
                searchProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onSearchFailed(String errorMessage) {
                searchProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCustomerDisplayFound(ServerInfo serverInfo) {
                requireActivity().runOnUiThread(() -> {
                    Log.d(TAG, "Customer display found: " + serverInfo.getServerID());
                    List<ServerInfo> searchResults = customerDisplayManager.getAvailableCustomerDisplays();
                    searchListAdapter.updateSearchResults(searchResults);
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