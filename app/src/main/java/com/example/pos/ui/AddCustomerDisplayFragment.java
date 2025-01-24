package com.example.pos.ui;

import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.example.customerdisplayhandler.helpers.InputValidationHelper;
import com.example.customerdisplayhandler.model.ServiceInfo;
import com.example.pos.MainActivity;
import com.example.pos.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


public class AddCustomerDisplayFragment extends DialogFragment {

    public static final String TAG = AddCustomerDisplayFragment.class.getSimpleName();
    private TextInputEditText nameEditText;
    private TextInputEditText ipAddressEditText;
    private ServiceInfo selectedServiceInfo;
    private String customerDisplayIPAddress;
    private String customerDisplayName;
    private Boolean isDarkMode = false;
    private MaterialButton pairButton;
    private MaterialSwitch darkModeSwitch;
    private TextInputLayout ipAddressInputLayout, nameInputLayout;
    private boolean isUpdating = false;

    public AddCustomerDisplayFragment() {
        // Required empty public constructor
    }

    public static AddCustomerDisplayFragment newInstance() {
        return new AddCustomerDisplayFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_add_customer_display, container, false);
//        ViewGroup childContainer = rootView.findViewById(R.id.main);
//        View childView = UiProvider.getAddCustomerDisplayView(inflater, container);
//        childContainer.addView(childView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameInputLayout = view.findViewById(R.id.customer_display_name_text_input_layout);
        nameEditText = view.findViewById(R.id.customer_display_name_edit_text);
        ipAddressInputLayout = view.findViewById(R.id.customer_display_ip_address_text_input_layout);
        ipAddressEditText = view.findViewById(R.id.customer_display_ip_address_edit_text);
        MaterialButton searchButton = view.findViewById(R.id.customer_display_search_button);
        searchButton.setOnClickListener(v -> showSearchCustomerDisplayFragment());
        pairButton = view.findViewById(R.id.pair_customer_display_button);
        darkModeSwitch = view.findViewById(R.id.customer_display_dark_mode_switch);

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isDarkMode = isChecked;
        });

        pairButton.setOnClickListener(v -> {
            if (InputValidationHelper.validateIpAddress(customerDisplayIPAddress,ipAddressInputLayout) && InputValidationHelper.validateName(customerDisplayName,nameInputLayout)) {
                if(selectedServiceInfo == null){
                    selectedServiceInfo = new ServiceInfo(null, customerDisplayName, customerDisplayIPAddress,null);
                }
                selectedServiceInfo.setDeviceName(customerDisplayName);
                selectedServiceInfo.setIpAddress(customerDisplayIPAddress);
                PairingFragment pairingFragment = PairingFragment.newInstance(selectedServiceInfo, isDarkMode);
                pairingFragment.show(getChildFragmentManager(), PairingFragment.TAG);
            }else{
                MainActivity mainActivity = (MainActivity) requireActivity();
                mainActivity.showToast("Please check the fields and try again");
            }
        });

        InputValidationHelper.addIpAddressWatcher(ipAddressEditText, ipAddressInputLayout, text -> customerDisplayIPAddress = text);

        InputValidationHelper.addNameWatcher(nameEditText, nameInputLayout, text -> customerDisplayName = text);

    }

    public void updateSelectedCustomerDisplay(ServiceInfo serviceInfo) {
        selectedServiceInfo = serviceInfo;
        Log.d(TAG, "Updating customer display: " + serviceInfo.getServerId());
        nameEditText.setText(serviceInfo.getDeviceName());
        ipAddressEditText.setText(serviceInfo.getIpAddress());
    }

    @Override
    public void onStart() {
        super.onStart();
        configureDialogWindow();
    }

    private void showSearchCustomerDisplayFragment() {
        SearchCustomerDisplayDialogFragment searchCustomerDisplayDialogFragment = SearchCustomerDisplayDialogFragment.newInstance();
        searchCustomerDisplayDialogFragment.show(requireActivity().getSupportFragmentManager(), SearchCustomerDisplayDialogFragment.TAG);
    }

    private void configureDialogWindow() {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.white);
        }
    }
}