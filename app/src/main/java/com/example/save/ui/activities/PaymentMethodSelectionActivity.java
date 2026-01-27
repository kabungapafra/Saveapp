package com.example.save.ui.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.save.R;
import com.example.save.databinding.ActivityPaymentMethodSelectionBinding;

public class PaymentMethodSelectionActivity extends AppCompatActivity {

    private ActivityPaymentMethodSelectionBinding binding;
    private SharedPreferences prefs;
    private String[] paymentMethods = {"MTN Mobile Money", "Airtel Money", "Bank Transfer", "Cash"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentMethodSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        setupListeners();
        setupPaymentMethodSpinner();
    }

    private void setupListeners() {
        binding.backButton.setOnClickListener(v -> finish());
        binding.btnSavePaymentMethod.setOnClickListener(v -> savePaymentMethod());
    }

    private void setupPaymentMethodSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, paymentMethods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerPaymentMethod.setAdapter(adapter);

        // Load current payment method
        String currentMethod = prefs.getString("default_payment_method", "MTN Mobile Money");
        int position = 0;
        for (int i = 0; i < paymentMethods.length; i++) {
            if (paymentMethods[i].equals(currentMethod)) {
                position = i;
                break;
            }
        }
        binding.spinnerPaymentMethod.setSelection(position);

        binding.spinnerPaymentMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                binding.tvSelectedMethod.setText("Selected: " + paymentMethods[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void savePaymentMethod() {
        int position = binding.spinnerPaymentMethod.getSelectedItemPosition();
        String selectedMethod = paymentMethods[position];

        prefs.edit()
                .putString("default_payment_method", selectedMethod)
                .apply();

        Toast.makeText(this, "Default payment method set to " + selectedMethod, 
                Toast.LENGTH_SHORT).show();
        
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
