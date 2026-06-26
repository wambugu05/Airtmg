package com.cashmoney.airtmg;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private TextInputLayout tilEmail, tilPassword;
    private ProgressBar progressBar;
    private DatabaseHelper dbHelper;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        dbHelper = new DatabaseHelper(this);

        tilEmail = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.loginProgressBar);

        // Instant validation on text change
        etEmail.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) tilEmail.setError(null);
            }
        });

        etPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) tilPassword.setError(null);
            }
        });

        btnLogin.setOnClickListener(v -> performLogin());

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.ivLogo).setOnClickListener(v -> showBiometricPrompt());
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            return;
        }
        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLoginState(false);
        
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String username = getUsernameFromLocal(email);
                        navigateToDashboard(username);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnLoginState(true);
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Login failed";
                        
                        if (errorMsg != null && errorMsg.contains("CONFIGURATION_NOT_FOUND")) {
                            errorMsg = "Firebase Auth error: Ensure 'Email/Password' is enabled in Firebase Console";
                        }

                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void btnLoginState(boolean enabled) {
        findViewById(R.id.btnLogin).setEnabled(enabled);
        findViewById(R.id.btnLogin).setAlpha(enabled ? 1.0f : 0.5f);
    }

    private String getUsernameFromLocal(String email) {
        if (email == null) return "User";
        Cursor cursor = dbHelper.getUserDataByEmail(email);
        String username = "User";
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex("username");
                if (index != -1) {
                    username = cursor.getString(index);
                }
            }
            cursor.close();
        }
        return username;
    }

    private void navigateToDashboard(String username) {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        intent.putExtra("USERNAME", username);
        startActivity(intent);
        finish();
    }

    private void showBiometricPrompt() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in with password once to enable biometrics", Toast.LENGTH_LONG).show();
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(LoginActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                navigateToDashboard(getUsernameFromLocal(user.getEmail()));
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Login")
                .setSubtitle("Log in using your fingerprint or face")
                .setNegativeButtonText("Use account password")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
    }
}
