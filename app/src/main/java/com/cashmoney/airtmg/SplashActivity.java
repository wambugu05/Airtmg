package com.cashmoney.airtmg;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                // User is signed in, check if we have their local data
                String email = currentUser.getEmail();
                String username = getUsernameFromLocal(email);
                
                Intent intent = new Intent(SplashActivity.this, DashboardActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            } else {
                // No user is signed in
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, 3000);
    }

    private String getUsernameFromLocal(String email) {
        if (email == null) return "User";
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Cursor cursor = dbHelper.getUserDataByEmail(email);
        String username = "User";
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
            }
            cursor.close();
        }
        return username;
    }
}
