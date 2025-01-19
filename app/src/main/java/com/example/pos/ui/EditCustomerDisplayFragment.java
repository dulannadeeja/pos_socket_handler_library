package com.example.pos.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.customerdisplayhandler.helpers.InputValidationHelper;
import com.example.customerdisplayhandler.model.CustomerDisplay;
import com.example.pos.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


public class EditCustomerDisplayFragment extends DialogFragment {
    public static final String TAG = EditCustomerDisplayFragment.class.getSimpleName();
    private static final String ARG_PARAM_CUSTOMER_DISPLAY = "customerDisplay";
    private CustomerDisplayViewModel customerDisplayViewModel;
    private CustomerDisplay customerDisplay;
    private TextInputLayout nameInputLayout;
    private TextInputEditText nameEditText;
    private TextInputLayout ipAddressInputLayout;
    private TextInputEditText ipAddressEditText;
    private MaterialSwitch darkModeSwitch;
    private MaterialSwitch displayActivationSwitch;
    private MaterialButton troubleshootingButton;
    private MaterialButton disconnectButton;
    private Boolean isDarkMode;
    private Boolean isActivated;
    private String customerDisplayName;
    private String customerDisplayIPAddress;
    private MaterialButton saveButton;
    private Boolean isUpdating = false;

    public EditCustomerDisplayFragment() {
        // Required empty public constructor
    }

    public static EditCustomerDisplayFragment newInstance(CustomerDisplay customerDisplay) {
        EditCustomerDisplayFragment fragment = new EditCustomerDisplayFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM_CUSTOMER_DISPLAY, customerDisplay);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            customerDisplay = (CustomerDisplay) getArguments().getSerializable(ARG_PARAM_CUSTOMER_DISPLAY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_customer_display, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewModels();
        initializeViews(view);
        setInitialData();
        setListeners();
    }

    private void initViewModels() {
        customerDisplayViewModel = new ViewModelProvider(requireActivity()).get(CustomerDisplayViewModel.class);
    }

    private void initializeViews(View view) {
        nameInputLayout = view.findViewById(R.id.customer_display_name_text_input_layout);
        nameEditText = view.findViewById(R.id.customer_display_name_edit_text);
        ipAddressInputLayout = view.findViewById(R.id.customer_display_ip_address_text_input_layout);
        ipAddressEditText = view.findViewById(R.id.customer_display_ip_address_edit_text);
        darkModeSwitch = view.findViewById(R.id.customer_display_dark_mode_switch);
        displayActivationSwitch = view.findViewById(R.id.customer_display_active_switch);
        saveButton = view.findViewById(R.id.customer_display_save_button);
        troubleshootingButton = view.findViewById(R.id.customer_display_troubleshoot_button);
        disconnectButton = view.findViewById(R.id.remove_customer_display_button);
    }
    private void setInitialData() {
        nameEditText.setText(customerDisplay.getCustomerDisplayName());
        ipAddressEditText.setText(customerDisplay.getCustomerDisplayIpAddress());
        darkModeSwitch.setChecked(customerDisplay.getIsDarkModeActivated());
        displayActivationSwitch.setChecked(customerDisplay.getIsActivated());
        isDarkMode = customerDisplay.getIsDarkModeActivated();
        darkModeSwitch.setChecked(isDarkMode);
        isActivated = customerDisplay.getIsActivated();
        customerDisplayName = customerDisplay.getCustomerDisplayName();
        customerDisplayIPAddress = customerDisplay.getCustomerDisplayIpAddress();
    }

    private void setListeners() {
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isDarkMode = isChecked;
        });

        displayActivationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isActivated = isChecked;
        });

        InputValidationHelper.addIpAddressWatcher(
                ipAddressEditText,
                ipAddressInputLayout,
                text -> customerDisplayIPAddress = text
        );

        InputValidationHelper.addNameWatcher(
                nameEditText,
                nameInputLayout,
                text -> customerDisplayName = text
        );

        saveButton.setOnClickListener(v -> onSaveButtonClick());

        troubleshootingButton.setOnClickListener(v -> onTroubleshootButtonClick());
        disconnectButton.setOnClickListener(v -> onDisconnectButtonClick());
    }

    private void onSaveButtonClick() {
        Boolean isNameValid = InputValidationHelper.validateName(customerDisplayName, nameInputLayout);
        Boolean isIpAddressValid = InputValidationHelper.validateIpAddress(customerDisplayIPAddress, ipAddressInputLayout);
        if (!isNameValid || !isIpAddressValid) {
            Toast.makeText(requireContext(), "Please fill in all fields correctly", Toast.LENGTH_SHORT).show();
            return;
        }
        CustomerDisplay updatedCustomerDisplay = new CustomerDisplay(
                customerDisplay.getCustomerDisplayID(),
                customerDisplayName,
                customerDisplayIPAddress,
                isActivated,
                isDarkMode
        );
        customerDisplayViewModel.onUpdateCustomerDisplay(updatedCustomerDisplay, this::dismiss);
    }

    private void onTroubleshootButtonClick() {
        TroubleshootDisplayFragment troubleshootDisplayFragment = TroubleshootDisplayFragment.newInstance(customerDisplay);
        troubleshootDisplayFragment.show(requireActivity().getSupportFragmentManager(), TroubleshootDisplayFragment.TAG);
    }

    private void onDisconnectButtonClick() {
        customerDisplayViewModel.disconnectCustomerDisplay(customerDisplay);
        dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();
        configureDialogWindow();
    }

    private void configureDialogWindow() {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.white);
        }
    }
}