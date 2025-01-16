package com.example.pos.ui;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.customerdisplayhandler.model.CustomerDisplay;
import com.example.customerdisplayhandler.utils.IJsonUtil;
import com.example.customerdisplayhandler.utils.JsonUtilImpl;
import com.example.pos.App;
import com.example.pos.R;
import com.example.pos.adapters.FailedDisplayListAdapter;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;

public class FailedCustomerDisplaysFragment extends DialogFragment {
    public static final String TAG = FailedCustomerDisplaysFragment.class.getSimpleName();
    private static final String ARG_FAILED_CUSTOMER_DISPLAYS = "failedCustomerDisplays";
    private List<CustomerDisplay> failedCustomerDisplays;
    private IJsonUtil jsonUtil;
    private TroubleshootViewModel troubleshootViewModel;
    private FailedDisplayListAdapter adapter;

    public FailedCustomerDisplaysFragment() {
        // Required empty public constructor
    }
    public static FailedCustomerDisplaysFragment newInstance(String json) {
        FailedCustomerDisplaysFragment fragment = new FailedCustomerDisplaysFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FAILED_CUSTOMER_DISPLAYS, json);
        fragment.setArguments(args);
        fragment.jsonUtil = new JsonUtilImpl();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String json = getArguments().getString(ARG_FAILED_CUSTOMER_DISPLAYS);
            TypeToken<List<CustomerDisplay>> typeToken = new TypeToken<>() {
            };
            failedCustomerDisplays = jsonUtil.toTypedObj(json, typeToken);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_failed_customer_displays, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        troubleshootViewModel = new ViewModelProvider(this).get(TroubleshootViewModel.class);
        troubleshootViewModel.setCustomerDisplayManager(((App) requireActivity().getApplication()).getCustomerDisplayManager());

        RecyclerView recyclerView = view.findViewById(R.id.rv_failed_displays);
        adapter = new FailedDisplayListAdapter(failedCustomerDisplays, this::showTroubleshootDisplayFragment);
        recyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
    }

    private void showTroubleshootDisplayFragment(CustomerDisplay customerDisplay) {
        TroubleshootDisplayFragment fragment = TroubleshootDisplayFragment.newInstance(customerDisplay);
        fragment.show(getParentFragmentManager(), TroubleshootDisplayFragment.TAG);
    }

    public void removeFailedDisplay(CustomerDisplay customerDisplay) {
        failedCustomerDisplays.remove(customerDisplay);

        if (failedCustomerDisplays.isEmpty()) {
            dismiss();
        } else {
            updateFailedDisplays();
        }
    }

    private void updateFailedDisplays() {
        String json = jsonUtil.toJson(failedCustomerDisplays);
        if(getArguments() != null) {
            getArguments().putString(ARG_FAILED_CUSTOMER_DISPLAYS, json);
        }
        adapter.updateFailedCustomerDisplays(failedCustomerDisplays);
    }
}