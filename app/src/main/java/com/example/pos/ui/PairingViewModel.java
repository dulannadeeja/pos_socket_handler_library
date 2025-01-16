package com.example.pos.ui;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.customerdisplayhandler.api.ICustomerDisplayManager;
import com.example.customerdisplayhandler.model.ServiceInfo;
import com.example.customerdisplayhandler.shared.OnPairingServerListener;
import com.example.pos.App;

import java.lang.ref.WeakReference;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class PairingViewModel extends ViewModel {

    private ICustomerDisplayManager customerDisplayManager;
    private final MutableLiveData<String> pairingStatus = new MutableLiveData<>();
    private Handler uiHandler;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public PairingViewModel() {
        super();
        uiHandler = new Handler(Looper.getMainLooper());
    }

    public LiveData<String> getPairingStatus() {
        return pairingStatus;
    }

    public void setCustomerDisplayManager(ICustomerDisplayManager customerDisplayManager) {
        this.customerDisplayManager = customerDisplayManager;
    }

    public void startPairing(ServiceInfo serviceInfo, OnPairingCompletedListener onPairingCompletedListener) {
        customerDisplayManager.startPairingCustomerDisplay(serviceInfo, new WeakPairingServerListener(this, onPairingCompletedListener));
    }

    public interface OnPairingCompletedListener {
        void onPairingCompleted(ServiceInfo serviceInfo);
    }

    private static class WeakPairingServerListener implements OnPairingServerListener {

        private final WeakReference<PairingViewModel> viewModelRef;
        private final WeakReference<OnPairingCompletedListener> onPairingCompletedListener;

        WeakPairingServerListener(PairingViewModel viewModel, OnPairingCompletedListener onPairingCompletedListener) {
            this.viewModelRef = new WeakReference<>(viewModel);
            this.onPairingCompletedListener = new WeakReference<>(onPairingCompletedListener);
        }

        @Override
        public void onPairingServerStarted() {
            PairingViewModel viewModel = viewModelRef.get();
            if (viewModel != null && viewModel.uiHandler != null) {
                viewModel.uiHandler.post(() -> {
                    viewModel.pairingStatus.setValue("Please wait while we looking for customer display...");
                });
            }
        }

        @Override
        public void onCustomerDisplayFound() {
            PairingViewModel viewModel = viewModelRef.get();
            if (viewModel != null && viewModel.uiHandler != null) {
                viewModel.uiHandler.postDelayed(() -> {
                    viewModel.pairingStatus.setValue("Customer display found, sending connection request...");
                }, 2000);
            }
        }

        @Override
        public void onConnectionRequestSent() {
            PairingViewModel viewModel = viewModelRef.get();
            if (viewModel != null && viewModel.uiHandler != null) {
                viewModel.uiHandler.postDelayed(() -> {
                    viewModel.pairingStatus.setValue("Connection request sent to customer display, waiting for approval...");
                }, 4000);
            }
        }

        @Override
        public void onConnectionRequestApproved(ServiceInfo serviceInfo) {
            PairingViewModel viewModel = viewModelRef.get();
            if (viewModel != null && viewModel.uiHandler != null) {
                viewModel.uiHandler.postDelayed(() -> {
                    viewModel.pairingStatus.setValue("Please wait while we establishing connection with customer display...");
                }, 2000);
            }
        }

        @Override
        public void onConnectionRequestRejected() {
            PairingViewModel viewModel = viewModelRef.get();
            if (viewModel != null && viewModel.uiHandler != null) {
                viewModel.uiHandler.postDelayed(() -> {
                    viewModel.pairingStatus.setValue("Connection request rejected by customer display...");
                }, 2000);
            }
        }

        @Override
        public void onSavedEstablishedConnection(ServiceInfo serviceInfo) {
            PairingViewModel viewModel = viewModelRef.get();
            if (viewModel != null && viewModel.uiHandler != null) {
                viewModel.uiHandler.postDelayed(() -> {
                    viewModel.pairingStatus.setValue("All set, customer display connected successfully...");
                    OnPairingCompletedListener listener = onPairingCompletedListener.get();
                    if (listener != null) {
                        listener.onPairingCompleted(serviceInfo);
                    }
                }, 2000);
            }
        }

        @Override
        public void onPairingServerFailed(String message) {
            PairingViewModel viewModel = viewModelRef.get();
            if (viewModel != null && viewModel.uiHandler != null) {
                viewModel.uiHandler.post(() -> {
                    viewModel.pairingStatus.setValue(message);
                });
            }
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
        uiHandler = null;
    }
}
