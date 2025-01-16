package com.example.pos.ui;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.customerdisplayhandler.api.ICustomerDisplayManager;
import com.example.customerdisplayhandler.model.CustomerDisplay;
import com.example.customerdisplayhandler.shared.OnTroubleshootListener;

import java.lang.ref.WeakReference;

public class TroubleshootViewModel extends ViewModel {
    private static final String TAG = TroubleshootViewModel.class.getSimpleName();
    private Handler uiHandler;
    private ICustomerDisplayManager customerDisplayManager;
    private final MutableLiveData<String> troubleshootingStatus;
    private WeakReference<OnTroubleshootListener> troubleshootListenerRef;

    public TroubleshootViewModel() {
        super();
        troubleshootingStatus = new MutableLiveData<>();
        uiHandler = new Handler(Looper.getMainLooper());
    }

    public LiveData<String> getTroubleshootingStatus() {
        return troubleshootingStatus;
    }

    public void setCustomerDisplayManager(ICustomerDisplayManager customerDisplayManager) {
        this.customerDisplayManager = customerDisplayManager;
    }

    public void startTroubleshooting(CustomerDisplay customerDisplay,OnTroubleshootCompleteListener onTroubleshootCompleteListener) {
        if (uiHandler == null) {
            uiHandler = new Handler(Looper.getMainLooper());
        }
        OnTroubleshootListener listener = new TroubleshootListener(onTroubleshootCompleteListener);
        troubleshootListenerRef = new WeakReference<>(listener);
        customerDisplayManager.startManualTroubleshooting(customerDisplay, listener);
    }

    public void stopTroubleshooting() {
        customerDisplayManager.stopManualTroubleshooting();
        uiHandler.removeCallbacksAndMessages(null);
        uiHandler = null;
    }

    private class TroubleshootListener implements OnTroubleshootListener {

        private WeakReference<OnTroubleshootCompleteListener> onTroubleshootCompleteListenerRef;

        public TroubleshootListener(OnTroubleshootCompleteListener onTroubleshootCompleteListener) {
            onTroubleshootCompleteListenerRef = new WeakReference<>(onTroubleshootCompleteListener);
        }

        @Override
        public void onScanningForCustomerDisplays() {
            uiHandler.post(() -> updateStatus("Looking for customer displays live on the network, please wait it may take up to 30 seconds..."));
        }

        @Override
        public void onCustomerDisplayFound() {
            uiHandler.postDelayed(() -> updateStatus("Customer display found. Attempting to connect..."), 1000);
        }

        @Override
        public void onAttemptingToConnect() {
            uiHandler.postDelayed(() -> updateStatus("Attempting to connect to customer display..."), 2000);
        }

        @Override
        public void onSavingCustomerDisplay() {
            uiHandler.postDelayed(() -> updateStatus("Saving updated connection info for customer display..."), 3000);
        }

        @Override
        public void onTroubleshootCompleted() {
            uiHandler.postDelayed(() -> {
                updateStatus("Troubleshooting completed successfully.");
                OnTroubleshootCompleteListener onTroubleshootCompleteListener = onTroubleshootCompleteListenerRef.get();
                if (onTroubleshootCompleteListener != null) {
                    onTroubleshootCompleteListener.onTroubleshootComplete();
                }
            }, 4000);
        }

        @Override
        public void onTroubleshootFailed(String message) {
            updateStatus(message);
        }

        private void updateStatus(String message) {
            troubleshootingStatus.postValue(message);
        }
    }

    public interface OnTroubleshootCompleteListener {
        void onTroubleshootComplete();
    }
}

