package com.example.pos;

import static androidx.core.app.PendingIntentCompat.getActivity;

import android.os.Bundle;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.customerdisplayhandler.api.CustomerDisplayManager;
import com.example.customerdisplayhandler.api.CustomerDisplayManagerImpl;
import com.example.pos.ui.AddCustomerDisplayFragment;
import com.example.pos.ui.CustomerDisplaySettingsDialogFragment;

public class MainActivity extends AppCompatActivity {
    private CustomerDisplayManager customerDisplayManager;

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
        customerDisplayButton.setOnClickListener(v -> showCustomerDisplaySettingsFragment());

    }

    private void showCustomerDisplaySettingsFragment() {
        CustomerDisplaySettingsDialogFragment customerDisplaySettingsDialogFragment = CustomerDisplaySettingsDialogFragment.newInstance();
        customerDisplaySettingsDialogFragment.show(getSupportFragmentManager(), "customer_display_settings");
    }

    private void showAddCustomerDisplayFragment() {
        AddCustomerDisplayFragment addCustomerDisplayFragment = AddCustomerDisplayFragment.newInstance();
        addCustomerDisplayFragment.show(getSupportFragmentManager(), "add_customer_display");
    }
}