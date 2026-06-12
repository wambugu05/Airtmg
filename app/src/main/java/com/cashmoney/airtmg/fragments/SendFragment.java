package com.cashmoney.airtmg.fragments;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
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

public class SendFragment extends Fragment {

    private EditText etRecipient, etAmount;
    private Button btnSend;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_send, container, false);

        etRecipient = view.findViewById(R.id.etRecipient);
        etAmount = view.findViewById(R.id.etAmount);
        btnSend = view.findViewById(R.id.btnSendMoney);
        dbHelper = new DatabaseHelper(getContext());

        btnSend.setOnClickListener(v -> {
            String recipient = etRecipient.getText().toString();
            String amountStr = etAmount.getText().toString();

            if (recipient.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter all details", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            showConfirmationDialog(recipient, amount);
        });

        return view;
    }

    private void showConfirmationDialog(String recipient, double amount) {
        new AlertDialog.Builder(getContext())
                .setTitle("Confirm Transaction")
                .setMessage("Are you sure you want to send $" + amount + " to @" + recipient + "?")
                .setPositiveButton("Send Now", (dialog, which) -> processTransaction(recipient, amount))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void processTransaction(String recipient, double amount) {
        String currentUser = getActivity().getIntent().getStringExtra("USERNAME");
        if (currentUser == null) currentUser = "DemoUser";

        if (recipient.equals(currentUser)) {
            Toast.makeText(getContext(), "You cannot send money to yourself", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = dbHelper.getUserData(currentUser);
        Cursor recipientCursor = dbHelper.getUserData(recipient);

        if (cursor.moveToFirst() && recipientCursor.moveToFirst()) {
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            double balance = cursor.getDouble(cursor.getColumnIndexOrThrow("balance"));

            int recipientId = recipientCursor.getInt(recipientCursor.getColumnIndexOrThrow("id"));
            double recipientBalance = recipientCursor.getDouble(recipientCursor.getColumnIndexOrThrow("balance"));

            if (balance >= amount) {
                dbHelper.updateBalance(userId, balance - amount);
                dbHelper.addTransaction(userId, "SEND", amount, "USD", "Sent to " + recipient);

                dbHelper.updateBalance(recipientId, recipientBalance + amount);
                dbHelper.addTransaction(recipientId, "RECEIVE", amount, "USD", "Received from " + currentUser);

                Toast.makeText(getContext(), "Transaction Successful", Toast.LENGTH_LONG).show();
                etRecipient.setText("");
                etAmount.setText("");
            } else {
                Toast.makeText(getContext(), "Insufficient balance", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Recipient not found", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
        recipientCursor.close();
    }
}
