package com.example.customerdisplayhandler.shared;

public interface OnTroubleshootListener {
    void onScanningForCustomerDisplays();
    void onCustomerDisplayFound();
    void onAttemptingToConnect();
    void onSavingCustomerDisplay();
    void onTroubleshootCompleted();
    void onTroubleshootFailed(String message);
}
