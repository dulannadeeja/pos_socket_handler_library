package com.example.customerdisplayhandler.shared;

public interface OnSilentTroubleshootListener {
    void onTroubleshootCompleted();
    void onTroubleshootFailed(String message);
}
