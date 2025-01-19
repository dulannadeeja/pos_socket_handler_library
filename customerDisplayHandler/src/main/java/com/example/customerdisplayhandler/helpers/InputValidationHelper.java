package com.example.customerdisplayhandler.helpers;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

public class InputValidationHelper {
    public static void addNameWatcher(EditText editText, TextInputLayout inputLayout, OnTextChangeListener listener) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = s.toString().trim();
                listener.onTextChanged(name);
                validateName(name, inputLayout);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    public static Boolean validateName(String customerDisplayName, TextInputLayout nameInputLayout) {
        if (customerDisplayName.isEmpty()) {
            nameInputLayout.setError("Looks like you forgot to enter a name");
            return false;
        } else {
            nameInputLayout.setError(null);
            return true;
        }
    }

    public static void addIpAddressWatcher(EditText editText, TextInputLayout inputLayout, OnTextChangeListener listener) {
        editText.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;

                isUpdating = true;
                String customerDisplayIPAddress = s.toString();
                String formattedIpAddress = formatIpAddress(customerDisplayIPAddress);
                listener.onTextChanged(formattedIpAddress);
                validateIpAddress(formattedIpAddress, inputLayout);
                editText.setText(formattedIpAddress);
                editText.setSelection(formattedIpAddress.length());
                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private static String formatIpAddress(String inputIpAddress) {
        String sanitizedInput = inputIpAddress.trim();
        // Replace any invalid characters (allow only numbers and ".")
        sanitizedInput = inputIpAddress.replaceAll("[^0-9.]", "");

        int length = sanitizedInput.length();

        // Ip address can't start with a dot
        if(sanitizedInput.startsWith(".")) {
            sanitizedInput = sanitizedInput.substring(1);
        }

        // Ip address can't start with a zero
        if(sanitizedInput.startsWith("0")){
            sanitizedInput = sanitizedInput.substring(1);
        }

        // Ip address cannot have more than 3 dots
        int numberOfUsedDots = sanitizedInput.length() - sanitizedInput.replace(".", "").length();
        if (numberOfUsedDots > 3) {
            sanitizedInput = sanitizedInput.substring(0, sanitizedInput.length() - 1);
        }

        // Ip address cannot have more than 3 digits between dots
        if(!sanitizedInput.endsWith(".")){
            int lastDotIndex = sanitizedInput.lastIndexOf(".");
            if(sanitizedInput.length() - lastDotIndex > 4){
                sanitizedInput = sanitizedInput.substring(0, sanitizedInput.length() - 1);
            }
        }
        return sanitizedInput;
    }

    public static Boolean validateIpAddress(String ipAddress, TextInputLayout ipAddressInputLayout) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddressInputLayout.setError("Looks like you forgot to enter an IP address.");
            return false;
        }
        String[] parts = ipAddress.split("\\.");
        if (parts.length != 4) {
            ipAddressInputLayout.setError("Looks like you entered an invalid IP address.");
            return false;
        }
        for (String s : parts) {
            int i = Integer.parseInt(s);
            if ((i < 0) || (i > 255)) {
                ipAddressInputLayout.setError("Looks like you entered an invalid IP address.");
                return false;
            }
        }
        ipAddressInputLayout.setError(null);
        return true;
    }

    public interface OnTextChangeListener {
        void onTextChanged(String text);
    }
}
