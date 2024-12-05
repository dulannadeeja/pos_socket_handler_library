package com.example.pos;

import android.os.Bundle;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.customerdisplayhandler.api.CustomerDisplayManager;
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

        initCustomerDisplayHandler();

        Button customerDisplayButton = findViewById(R.id.go_to_customer_display_btn);
        customerDisplayButton.setOnClickListener(v -> showCustomerDisplayFragment());

    }

    private void showCustomerDisplayFragment() {
        CustomerDisplaySettingsDialogFragment customerDisplaySettingsDialogFragment = CustomerDisplaySettingsDialogFragment.newInstance(customerDisplayManager.getCustomerDisplaySettingsChildFragment());
        customerDisplaySettingsDialogFragment.show(getSupportFragmentManager(), "customer_display_settings");
    }

    private void initCustomerDisplayHandler() {
        customerDisplayManager = CustomerDisplayManager.newInstance();
    }
}