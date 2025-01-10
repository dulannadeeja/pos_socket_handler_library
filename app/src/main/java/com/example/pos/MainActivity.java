package com.example.pos;

import android.os.Bundle;
import android.util.Pair;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.customerdisplayhandler.api.ICustomerDisplayManager;
import com.example.customerdisplayhandler.core.network.NetworkServiceDiscoveryManagerImpl;
import com.example.customerdisplayhandler.model.CustomerDisplay;
import com.example.pos.ui.AddCustomerDisplayFragment;
import com.example.pos.ui.CustomerDisplaySettingsDialogFragment;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ICustomerDisplayManager customerDisplayManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        App app = (App) getApplication();
        customerDisplayManager = app.getCustomerDisplayManager();

        Button customerDisplayButton = findViewById(R.id.go_to_customer_display_settings);
        NetworkServiceDiscoveryManagerImpl networkServiceDiscoveryManagerImpl = new NetworkServiceDiscoveryManagerImpl(getApplicationContext());
        customerDisplayButton.setOnClickListener(v -> {
            showCustomerDisplaySettingsFragment();
        });

        Button sendUpdatesButton = findViewById(R.id.send_to_customer_display);
        sendUpdatesButton.setOnClickListener(v -> {
            sendDisplayUpdates("test message");
        });
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showCustomerDisplaySettingsFragment() {
        CustomerDisplaySettingsDialogFragment customerDisplaySettingsDialogFragment = CustomerDisplaySettingsDialogFragment.newInstance();
        customerDisplaySettingsDialogFragment.show(getSupportFragmentManager(), CustomerDisplaySettingsDialogFragment.TAG);
    }

    private void showAddCustomerDisplayFragment() {
        AddCustomerDisplayFragment addCustomerDisplayFragment = AddCustomerDisplayFragment.newInstance();
        addCustomerDisplayFragment.show(getSupportFragmentManager(), "add_customer_display");
    }

    private void sendDisplayUpdates(String message){
        customerDisplayManager.sendUpdatesToCustomerDisplays("test message", new ICustomerDisplayManager.OnSendUpdatesListener() {
            @Override
            public void onUpdatesSent() {
                runOnUiThread(() -> showToast("Updates sent successfully"));
            }

            @Override
            public void onUpdatesSendFailed(List<Pair<CustomerDisplay, String>> errors) {
                runOnUiThread(() -> showToast("Failed to send updates"));
            }
        });
    }
}