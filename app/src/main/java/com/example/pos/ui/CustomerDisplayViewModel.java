package com.example.pos.ui;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.customerdisplayhandler.api.ICustomerDisplayManager;
import com.example.customerdisplayhandler.model.CustomerDisplay;

import java.util.List;

public class CustomerDisplayViewModel extends ViewModel {
    private MutableLiveData<Boolean> isActivationChangePending = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> isFetchingConnectedDisplays = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> isDisconnectingDisplay = new MutableLiveData<>(false);
    private ICustomerDisplayManager customerDisplayManager;
    private final MutableLiveData<List<CustomerDisplay>> pairedDisplays = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();

    public void setCustomerDisplayManager(ICustomerDisplayManager customerDisplayManager) {
        this.customerDisplayManager = customerDisplayManager;
    }

    public void toggleCustomerDisplayActivation(CustomerDisplay customerDisplay, OnSwitchToggleListener onSwitchToggleListener) {
        isActivationChangePending.setValue(true);
        customerDisplayManager.toggleCustomerDisplayActivation(customerDisplay.getCustomerDisplayID(), new ICustomerDisplayManager.OnCustomerDisplayActivationToggleListener() {
            @Override
            public void onCustomerDisplayActivated() {
                showToast(customerDisplay.getCustomerDisplayName() + " will now receive updates");
                refreshConnectedDisplays();
                isActivationChangePending.setValue(false);
            }

            @Override
            public void onCustomerDisplayDeactivated() {
                showToast(customerDisplay.getCustomerDisplayName() + " will no longer receive updates");
                refreshConnectedDisplays();
                isActivationChangePending.setValue(false);
            }

            @Override
            public void onCustomerDisplayActivationToggleFailed(String errorMessage) {
                showToast(errorMessage);
                isActivationChangePending.setValue(false);
                onSwitchToggleListener.onSwitchFailed();
                refreshConnectedDisplays();
            }
        });
    }

    public void fetchConnectedDisplays() {
        isFetchingConnectedDisplays.setValue(true);
        customerDisplayManager.getConnectedDisplays(new ICustomerDisplayManager.GetConnectedDisplaysListener() {
            @Override
            public void onConnectedDisplaysReceived(List<CustomerDisplay> connectedDisplays) {
                pairedDisplays.setValue(connectedDisplays);
                isFetchingConnectedDisplays.setValue(false);
            }

            @Override
            public void onConnectedDisplaysReceiveFailed(String errorMessage) {
                showToast("Error occurred while getting connected displays");
                isFetchingConnectedDisplays.setValue(false);
            }
        });
    }

    public void disconnectCustomerDisplay(CustomerDisplay customerDisplay){
        customerDisplayManager.removeConnectedDisplay(customerDisplay.getCustomerDisplayID(), new ICustomerDisplayManager.RemoveCustomerDisplayListener() {
            @Override
            public void onCustomerDisplayRemoved() {
                showToast(customerDisplay.getCustomerDisplayName() + " removed successfully");
                refreshConnectedDisplays();
            }

            @Override
            public void onCustomerDisplayRemoveFailed(String errorMessage) {
                showToast("Error occurred while removing " + customerDisplay.getCustomerDisplayName());
            }
        });
    }

    public void onUpdateCustomerDisplay(CustomerDisplay customerDisplay, OnUpdateDisplayListener onUpdateDisplayListener) {
        customerDisplayManager.updateCustomerDisplay(customerDisplay, new ICustomerDisplayManager.OnUpdateDisplayListener() {
            @Override
            public void onDisplayUpdated() {
                showToast(customerDisplay.getCustomerDisplayName() + " updated successfully");
                refreshConnectedDisplays();
                onUpdateDisplayListener.onDisplayUpdateComplete();
            }

            @Override
            public void onUpdateDisplayFailed(String errorMessage) {
                showToast(errorMessage);
            }
        });
    }

    public void refreshConnectedDisplays() {
        fetchConnectedDisplays();
    }

    private void showToast(String message) {
        toastMessage.setValue(message);
    }

    public MutableLiveData<List<CustomerDisplay>> getPairedDisplays() {
        return pairedDisplays;
    }

    public MutableLiveData<Boolean> getIsFetchingConnectedDisplays() {
        return isFetchingConnectedDisplays;
    }

    public MutableLiveData<Boolean> getIsActivationChangePending() {
        return isActivationChangePending;
    }

    public MutableLiveData<Boolean> getIsDisconnectingDisplay() {
        return isDisconnectingDisplay;
    }

    public MutableLiveData<String> getToastMessage() {
        return toastMessage;
    }

    public interface OnSwitchToggleListener {
        void onSwitchFailed();
    }

    public interface OnUpdateDisplayListener {
        void onDisplayUpdateComplete();
    }

}
