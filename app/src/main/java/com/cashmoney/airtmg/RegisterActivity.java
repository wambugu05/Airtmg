package com.cashmoney.airtmg;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword;
    private TextInputLayout tilUsername, tilEmail, tilPassword;
    private ProgressBar progressBar;
    private DatabaseHelper dbHelper;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        dbHelper = new DatabaseHelper(this);

        tilUsername = findViewById(R.id.tilRegUsername);
        tilEmail = findViewById(R.id.tilRegEmail);
        tilPassword = findViewById(R.id.tilRegPassword);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.registerProgressBar);

        setupInteraction();

        btnRegister.setOnClickListener(v -> performRegistration());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void setupInteraction() {
        etUsername.addTextChangedListener(new SimpleTextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) tilUsername.setError(null);
            }
        });
        etEmail.addTextChangedListener(new SimpleTextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) tilEmail.setError(null);
            }
        });
        etPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 6) tilPassword.setError(null);
            }
        });
    }

    private void performRegistration() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty()) {
            tilUsername.setError("Username required");
            return;
        }
        if (email.isEmpty()) {
            tilEmail.setError("Email required");
            return;
        }
        if (password.length() < 6) {
            tilPassword.setError("Minimum 6 characters");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        if (dbHelper.addUser(username, password, email)) {
                            Toast.makeText(RegisterActivity.this, "Welcome to Airtmg!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(RegisterActivity.this, "Local profile error", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                        Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
    }
}
