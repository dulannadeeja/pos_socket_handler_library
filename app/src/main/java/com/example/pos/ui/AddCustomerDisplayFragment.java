package com.example.pos.ui;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.example.customerdisplayhandler.model.ServiceInfo;
import com.example.customerdisplayhandler.ui.UiProvider;
import com.example.pos.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


public class AddCustomerDisplayFragment extends DialogFragment {

    public static final String TAG = AddCustomerDisplayFragment.class.getSimpleName();
    private TextInputEditText nameEditText;
    private TextInputEditText ipAddressEditText;
    private ServiceInfo selectedServiceInfo;
    private String customerDisplayIPAddress;
    private String customerDisplayName;
    private Boolean isDarkMode;
    private MaterialButton pairButton;
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

        pairButton.setOnClickListener(v -> {
            if (customerDisplayIPAddress != null && !customerDisplayIPAddress.isEmpty() && customerDisplayName != null && !customerDisplayName.isEmpty()) {
                if(selectedServiceInfo == null){
                    selectedServiceInfo = new ServiceInfo(null, customerDisplayName, customerDisplayIPAddress,null);
                }
                selectedServiceInfo.setDeviceName(customerDisplayName);
                selectedServiceInfo.setIpAddress(customerDisplayIPAddress);
                PairingFragment pairingFragment = PairingFragment.newInstance(selectedServiceInfo);
                pairingFragment.show(getChildFragmentManager(), PairingFragment.TAG);
            }else{
                validateIpAddress(customerDisplayIPAddress);
                validateName(customerDisplayName);
            }
        });

        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                customerDisplayName = s.toString();
                validateName(customerDisplayName);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ipAddressEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) {
                    return;
                }

                isUpdating = true;

                customerDisplayIPAddress = s.toString();
                String formattedIpAddress = formatIpAddress(customerDisplayIPAddress);

                if (formattedIpAddress.isEmpty()) {
                    updateIpAddressField("");
                    return;
                }

                if (formattedIpAddress.length() == 1) {
                    if (formattedIpAddress.equals("0") || formattedIpAddress.equals(".")) {
                        updateIpAddressField("");
                        return;
                    }
                    updateIpAddressField(formattedIpAddress);
                    return;
                }

                String lastChar = formattedIpAddress.substring(formattedIpAddress.length() - 1);
                String nextToLastChar = formattedIpAddress.substring(formattedIpAddress.length() - 2, formattedIpAddress.length() - 1);
                Boolean isLastCharDot = lastChar.equals(".");
                Boolean isNextToLastCharDot = nextToLastChar.equals(".");

                if (isLastCharDot && isNextToLastCharDot) {
                    formattedIpAddress = formattedIpAddress.substring(0, formattedIpAddress.length() - 1);
                }

                if(!isLastCharDot){
                    int lastDotIndex = formattedIpAddress.lastIndexOf(".");
                    if(formattedIpAddress.length() - lastDotIndex > 4){
                        formattedIpAddress = formattedIpAddress.substring(0, formattedIpAddress.length() - 1);
                    }
                }

                int numberOfUsedDots = formattedIpAddress.length() - formattedIpAddress.replace(".", "").length();
                if (numberOfUsedDots > 3) {
                    formattedIpAddress = formattedIpAddress.substring(0, formattedIpAddress.length() - 1);
                }

                updateIpAddressField(formattedIpAddress);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void updateIpAddressField(String ipAddress) {
        ipAddressEditText.setText(ipAddress);
        ipAddressEditText.setSelection(ipAddress.length());
        isUpdating = false;
        validateIpAddress(ipAddress);
    }

    private String formatIpAddress(String inputIpAddress) {
        String sanitizedInput = inputIpAddress.trim();
        // Replace any invalid characters (allow only numbers and ".")
        sanitizedInput = inputIpAddress.replaceAll("[^0-9.]", "");

        int length = sanitizedInput.length();
        if (length > 0) {
            // Remove any leading zeros
            sanitizedInput = sanitizedInput.replaceFirst("^0+(?!$)", "");
        }
        return sanitizedInput;
    }

    private void validateIpAddress(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddressInputLayout.setError("Looks like you forgot to enter an IP address.");
            return;
        }
        String[] parts = ipAddress.split("\\.");
        if (parts.length != 4) {
            ipAddressInputLayout.setError("Looks like you entered an invalid IP address.");
            return;
        }
        for (String s : parts) {
            int i = Integer.parseInt(s);
            if ((i < 0) || (i > 255)) {
                ipAddressInputLayout.setError("Looks like you entered an invalid IP address.");
                return;
            }
        }
        ipAddressInputLayout.setError(null);
    }

    private void validateName(String name) {
        if (name == null || name.isEmpty()) {
            nameInputLayout.setError("Looks like you forgot to enter a name.");
        } else {
            nameInputLayout.setError(null);
        }
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