package Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.widget.EditText;
import androidx.appcompat.widget.SwitchCompat;
import com.example.save.R;

public class SettingsActivity extends AppCompatActivity {

    private ImageView backButton;
    private CardView btnLogout;

    // Admin Configuration
    private EditText etContributionAmount, etLatePenalty;
    private SwitchCompat switchRandomQueue, switchEnforceEligibility, switchRequire2FA;

    // Account
    private LinearLayout btnProfileInfo, btnChangePassword, btnLanguage;

    // Notifications
    private LinearLayout btnNotificationMethod;

    // Security
    private LinearLayout btnAutoLock;

    // Payment
    private LinearLayout btnDefaultPayment;

    // Group Info
    private LinearLayout btnGroupDetails, btnViewMembers, btnContactAdmin;

    // Data
    private LinearLayout btnDownloadData, btnClearCache;

    // About
    private LinearLayout btnHelpSupport, btnTOS, btnPrivacyPolicy, btnRate, btnShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        setupListeners();
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        btnLogout = findViewById(R.id.btnLogout);

        // Admin Configuration
        etContributionAmount = findViewById(R.id.etContributionAmount);
        etLatePenalty = findViewById(R.id.etLatePenalty);
        switchRandomQueue = findViewById(R.id.switchRandomQueue);
        switchEnforceEligibility = findViewById(R.id.switchEnforceEligibility);
        switchRequire2FA = findViewById(R.id.switchRequire2FA);

        // Account
        btnProfileInfo = findViewById(R.id.btnProfileInfo);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLanguage = findViewById(R.id.btnLanguage);

        // Notifications
        btnNotificationMethod = findViewById(R.id.btnNotificationMethod);

        // Security
        btnAutoLock = findViewById(R.id.btnAutoLock);

        // Payment
        btnDefaultPayment = findViewById(R.id.btnDefaultPayment);

        // Group Info
        btnGroupDetails = findViewById(R.id.btnGroupDetails);
        btnViewMembers = findViewById(R.id.btnViewMembers);
        btnContactAdmin = findViewById(R.id.btnContactAdmin);

        // Data
        btnDownloadData = findViewById(R.id.btnDownloadData);
        btnClearCache = findViewById(R.id.btnClearCache);

        // About
        btnHelpSupport = findViewById(R.id.btnHelpSupport);
        btnTOS = findViewById(R.id.btnTOS);
        btnPrivacyPolicy = findViewById(R.id.btnPrivacyPolicy);
        btnRate = findViewById(R.id.btnRate);
        btnShare = findViewById(R.id.btnShare);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        btnLogout.setOnClickListener(v -> {
            // TODO: Implement actual logout logic (clear prefs, intent to LoginActivity)
            Toast.makeText(this, "Logout Clicked", Toast.LENGTH_SHORT).show();
        });

        // Listeners for clickable items (Toasts for now)
        View.OnClickListener notImplementedListener = v -> Toast
                .makeText(this, "Feature coming soon", Toast.LENGTH_SHORT).show();

        btnProfileInfo.setOnClickListener(notImplementedListener);
        btnChangePassword.setOnClickListener(notImplementedListener);
        btnLanguage.setOnClickListener(notImplementedListener);
        btnNotificationMethod.setOnClickListener(notImplementedListener);
        btnAutoLock.setOnClickListener(notImplementedListener);
        btnDefaultPayment.setOnClickListener(notImplementedListener);
        btnGroupDetails.setOnClickListener(notImplementedListener);
        btnViewMembers.setOnClickListener(notImplementedListener);
        btnContactAdmin.setOnClickListener(notImplementedListener);
        btnDownloadData.setOnClickListener(notImplementedListener);
        btnClearCache.setOnClickListener(v -> Toast.makeText(this, "Cache Cleared", Toast.LENGTH_SHORT).show());
        btnHelpSupport.setOnClickListener(notImplementedListener);
        btnTOS.setOnClickListener(notImplementedListener);
        btnPrivacyPolicy.setOnClickListener(notImplementedListener);
        btnRate.setOnClickListener(v -> Toast.makeText(this, "Thank you for rating!", Toast.LENGTH_SHORT).show());
        btnShare.setOnClickListener(v -> Toast.makeText(this, "Sharing...", Toast.LENGTH_SHORT).show());
    }
}
