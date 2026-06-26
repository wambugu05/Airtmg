package com.cashmoney.airtmg.fragments;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cashmoney.airtmg.DatabaseHelper;
import com.cashmoney.airtmg.R;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class SendFragment extends Fragment {

    private EditText etRecipient, etAmount;
    private TextInputLayout tilRecipient, tilAmount;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_send, container, false);

        tilRecipient = view.findViewById(R.id.tilRecipient);
        tilAmount = view.findViewById(R.id.tilAmount);
        etRecipient = view.findViewById(R.id.etRecipient);
        etAmount = view.findViewById(R.id.etAmount);
        Button btnSend = view.findViewById(R.id.btnSendMoney);
        dbHelper = new DatabaseHelper(getContext());

        // Interaction: Auto-clear errors on typing
        etRecipient.addTextChangedListener(new SimpleTextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilRecipient.setError(null);
            }
        });
        etAmount.addTextChangedListener(new SimpleTextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilAmount.setError(null);
            }
        });

        btnSend.setOnClickListener(v -> validateAndSend());

        return view;
    }

    private void validateAndSend() {
        String recipient = etRecipient.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();

        if (recipient.isEmpty()) {
            tilRecipient.setError("Recipient is required");
            return;
        }
        if (amountStr.isEmpty()) {
            tilAmount.setError("Amount is required");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                tilAmount.setError("Enter a valid amount");
                return;
            }
            showConfirmationDialog(recipient, amount);
        } catch (NumberFormatException e) {
            tilAmount.setError("Invalid number format");
        }
    }

    private void showConfirmationDialog(String recipient, double amount) {
        new AlertDialog.Builder(getContext())
                .setTitle("Confirm Payment")
                .setMessage("Authorize transfer of $" + amount + " to @" + recipient + "?")
                .setPositiveButton("Confirm", (dialog, which) -> processTransaction(recipient, amount))
                .setNegativeButton("Review", null)
                .show();
    }

    private void processTransaction(String recipient, double amount) {
        String currentUser = null;
        if (getActivity() != null && getActivity().getIntent() != null) {
            currentUser = getActivity().getIntent().getStringExtra("USERNAME");
        }
        if (currentUser == null) currentUser = "DemoUser";

        if (Objects.equals(recipient, currentUser)) {
            tilRecipient.setError("You cannot send to yourself");
            return;
        }

        Cursor cursor = dbHelper.getUserData(currentUser);
        Cursor recipientCursor = dbHelper.getUserData(recipient);

        if (cursor != null && recipientCursor != null && cursor.moveToFirst() && recipientCursor.moveToFirst()) {
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            double balance = cursor.getDouble(cursor.getColumnIndexOrThrow("balance"));

            int recipientId = recipientCursor.getInt(recipientCursor.getColumnIndexOrThrow("id"));
            double recipientBalance = recipientCursor.getDouble(recipientCursor.getColumnIndexOrThrow("balance"));

            if (balance >= amount) {
                dbHelper.updateBalance(userId, balance - amount);
                dbHelper.addTransaction(userId, "SEND", amount, "USD", "Paid @" + recipient);

                dbHelper.updateBalance(recipientId, recipientBalance + amount);
                dbHelper.addTransaction(recipientId, "RECEIVE", amount, "USD", "Received from @" + currentUser);

                Toast.makeText(getContext(), "Payment Successful!", Toast.LENGTH_LONG).show();
                etRecipient.setText("");
                etAmount.setText("");
            } else {
                tilAmount.setError("Insufficient balance");
            }
        } else {
            tilRecipient.setError("User not found");
        }
        if (cursor != null) cursor.close();
        if (recipientCursor != null) recipientCursor.close();
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
    }
}
