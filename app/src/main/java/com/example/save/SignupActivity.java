package com.example.save;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
    }

    public void onloginClick(View view) {
        Intent intent = new Intent(this, AdminregActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_out_right1, R.anim.slide_in_left1);
    }


    @Override
    public void finish () {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left1, R.anim.slide_out_right1);
    }
}