package com.example.save.ui.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.save.R;
import com.example.save.databinding.ActivityLanguageSelectionBinding;

public class LanguageSelectionActivity extends AppCompatActivity {

    private ActivityLanguageSelectionBinding binding;
    private SharedPreferences prefs;
    private String[] languages = {"English", "Luganda", "Kiswahili", "Runyoro", "Acholi"};
    private String[] languageCodes = {"en", "lg", "sw", "nyo", "ach"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLanguageSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        setupListeners();
        setupLanguageSpinner();
    }

    private void setupListeners() {
        binding.backButton.setOnClickListener(v -> finish());
        binding.btnSaveLanguage.setOnClickListener(v -> saveLanguage());
    }

    private void setupLanguageSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerLanguage.setAdapter(adapter);

        // Load current language
        String currentLang = prefs.getString("selected_language", "en");
        int position = 0;
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(currentLang)) {
                position = i;
                break;
            }
        }
        binding.spinnerLanguage.setSelection(position);

        binding.spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                binding.tvSelectedLanguage.setText("Selected: " + languages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void saveLanguage() {
        int position = binding.spinnerLanguage.getSelectedItemPosition();
        String selectedLanguage = languages[position];
        String selectedCode = languageCodes[position];

        prefs.edit()
                .putString("selected_language", selectedCode)
                .putString("selected_language_name", selectedLanguage)
                .apply();

        Toast.makeText(this, "Language changed to " + selectedLanguage + ". Restart app to apply changes.", 
                Toast.LENGTH_LONG).show();
        
        // Note: Full language implementation would require string resources for each language
        // This is a basic implementation that saves the preference
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
