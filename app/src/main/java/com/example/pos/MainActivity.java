package com.example.pos;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.customerdisplayhandler.api.ICustomerDisplayManager;
import com.example.customerdisplayhandler.core.network.NetworkServiceDiscoveryManagerImpl;
import com.example.pos.ui.AddCustomerDisplayFragment;
import com.example.pos.ui.CustomerDisplaySettingsDialogFragment;

public class MainActivity extends AppCompatActivity {
    private ICustomerDisplayManager ICustomerDisplayManager;

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
        ICustomerDisplayManager = app.getCustomerDisplayManager();

        Button customerDisplayButton = findViewById(R.id.go_to_customer_display_settings);
        NetworkServiceDiscoveryManagerImpl networkServiceDiscoveryManagerImpl = new NetworkServiceDiscoveryManagerImpl(getApplicationContext());
        customerDisplayButton.setOnClickListener(v -> {
            showCustomerDisplaySettingsFragment();
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
}