package com.example.save;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class AdminregActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminreg);
    }

        public void onsingupClick (View view){
            Intent intent = new Intent(this, SignupActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left1, R.anim.slide_out_right1);
        }

        @Override
        public void finish () {
            super.finish();
            overridePendingTransition(R.anim.slide_in_left1, R.anim.slide_out_right1);
        }
    }