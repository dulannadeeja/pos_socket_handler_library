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
    private HandlerThread handlerThread;
    private Handler backgroundHandler;
    private Handler uiHandler;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public PairingViewModel() {
        super();
        handlerThread = new HandlerThread("PairingBackgroundThread");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper());
        uiHandler = new Handler(Looper.getMainLooper());
    }

    public LiveData<String> getPairingStatus() {
        return pairingStatus;
    }

    public void setCustomerDisplayManager(ICustomerDisplayManager customerDisplayManager) {
        this.customerDisplayManager = customerDisplayManager;
    }

    public void startPairing(ServiceInfo serviceInfo) {
        compositeDisposable.add(customerDisplayManager.startPairingCustomerDisplay(serviceInfo, new WeakPairingServerListener(this))
                .subscribe(() -> {
                    Log.d("PairingViewModel", "Pairing server started");
                }, throwable -> {
                    Log.e("PairingViewModel", "Pairing server failed", throwable);
                }));
    }

    private static class WeakPairingServerListener implements OnPairingServerListener {

        private final WeakReference<PairingViewModel> viewModelRef;

        WeakPairingServerListener(PairingViewModel viewModel) {
            this.viewModelRef = new WeakReference<>(viewModel);
        }

        @Override
        public void onPairingServerStarted() {
            PairingViewModel viewModel = viewModelRef.get();
            if (viewModel != null) {
                viewModel.uiHandler.post(() -> {
                    viewModel.pairingStatus.setValue("Searching for customer display...");
                });
            }
        }

        @Override
        public void onConnectionRequestSent() {
            PairingViewModel viewModel = viewModelRef.get();
            if (viewModel != null) {
                viewModel.uiHandler.postDelayed(() -> {
                    viewModel.pairingStatus.setValue("Connection request sent, waiting for approval...");
                }, 2000);
            }
        }

        @Override
        public void onConnectionRequestApproved(ServiceInfo serviceInfo) {
            PairingViewModel viewModel = viewModelRef.get();
            if (viewModel != null) {
                viewModel.uiHandler.postDelayed(() -> {
                    viewModel.pairingStatus.setValue("Connection approval received from customer display...");
                    viewModel.onCustomerDisplayConnected(serviceInfo);
                }, 2000);
            }
        }

        @Override
        public void onConnectionRequestRejected() {
            PairingViewModel viewModel = viewModelRef.get();
            if (viewModel != null) {
                viewModel.uiHandler.post(() -> {
                    viewModel.pairingStatus.setValue("Connection request rejected by customer display...");
                });
            }
        }

        @Override
        public void onPairingServerFailed(String message) {
            PairingViewModel viewModel = viewModelRef.get();
            if (viewModel != null) {
                viewModel.uiHandler.post(() -> {
                    viewModel.pairingStatus.setValue(message);
                });
            }
        }
    }

    private void onCustomerDisplayConnected(ServiceInfo serviceInfo) {
        // Handle the display connection logic in the ViewModel
        // Notify the fragment or activity as necessary
    }

    public void stopPairing() {
        if (handlerThread != null) {
            handlerThread.quitSafely();
            handlerThread = null;
        }
        backgroundHandler = null;
        uiHandler = null;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
        stopPairing();
    }
}
