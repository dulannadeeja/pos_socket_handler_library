package com.example.pos.ui;

import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.example.customerdisplayhandler.ui.UiProvider;
import com.example.customerdisplayhandler.ui.callbacks.AddNewDisplayFabListener;
import com.example.pos.R;

public class CustomerDisplaySettingsDialogFragment extends DialogFragment {

    private static final String TAG = CustomerDisplaySettingsDialogFragment.class.getSimpleName();
    private AddNewDisplayFabListener addNewDisplayFabListener;

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
    }

    @Override
    public void onStart() {
        super.onStart();
        configureDialogWindow();
    }

    private void showAddCustomerDisplayFragment() {
        AddCustomerDisplayFragment addCustomerDisplayFragment = AddCustomerDisplayFragment.newInstance();
        addCustomerDisplayFragment.show(getChildFragmentManager(), AddCustomerDisplayFragment.TAG);
    }

    private void configureDialogWindow() {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.white);
        }
    }
}