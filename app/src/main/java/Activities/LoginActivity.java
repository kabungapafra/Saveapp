package Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.save.R;

public class LoginActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    /**
     * Handle Admin button click from XML
     */
    public void onAdminClick(View view) {
        Intent intent = new Intent(this, AdminregActivity.class);
        startActivity(intent);
    }

    /**
     * Handle Member button click from XML
     */
    public void onMemberClick(View view) {
        Intent intent = new Intent(this, MemberregActivity.class);
        startActivity(intent);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left1, R.anim.slide_out_right1);
    }
}