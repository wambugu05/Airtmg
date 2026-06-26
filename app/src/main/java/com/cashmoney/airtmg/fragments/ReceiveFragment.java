package com.cashmoney.airtmg.fragments;

import android.app.AlertDialog;
import android.database.Cursor;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cashmoney.airtmg.DatabaseHelper;
import com.cashmoney.airtmg.R;

import java.util.Locale;

public class ReceiveFragment extends Fragment {

    private TextView tvUserHandle;
    private DatabaseHelper dbHelper;
    private String currentUsername;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receive, container, false);

        tvUserHandle = view.findViewById(R.id.tvUserHandle);
        Button btnShare = view.findViewById(R.id.btnShareDetails);
        Button btnDeposit = view.findViewById(R.id.btnDirectDeposit);
        dbHelper = new DatabaseHelper(getContext());

        if (getActivity() != null && getActivity().getIntent() != null) {
            currentUsername = getActivity().getIntent().getStringExtra("USERNAME");
        }
        if (currentUsername == null) currentUsername = "User";

        tvUserHandle.setText(String.format(Locale.getDefault(), "@%s", currentUsername));

        btnShare.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "Quick-pay me on Airtmg! My handle is: " + tvUserHandle.getText().toString());
            startActivity(Intent.createChooser(intent, "Secure Share"));
        });

        btnDeposit.setOnClickListener(v -> showDepositDialog());

        return view;
    }

    private void showDepositDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_send, null);
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        // Reuse some Send UI for a simple deposit dialog
        dialogView.findViewById(R.id.tilRecipient).setVisibility(View.GONE);
        
        new AlertDialog.Builder(getContext())
                .setTitle("Direct Deposit")
                .setMessage("Simulate receiving cash from an external transfer service (Card/Bank/Global Pay).")
                .setView(dialogView)
                .setPositiveButton("Receive Cash", (dialog, which) -> {
                    String amountStr = etAmount.getText().toString();
                    if (!amountStr.isEmpty()) {
                        processDirectInflow(Double.parseDouble(amountStr));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void processDirectInflow(double amount) {
        Cursor cursor = dbHelper.getUserData(currentUsername);
        if (cursor != null && cursor.moveToFirst()) {
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            double currentBalance = cursor.getDouble(cursor.getColumnIndexOrThrow("balance"));

            // Update balance and record as external receipt
            dbHelper.updateBalance(userId, currentBalance + amount);
            dbHelper.addTransaction(userId, "RECEIVE", amount, "USD", "Direct Deposit via External Service");

            Toast.makeText(getContext(), "Funds deposited successfully!", Toast.LENGTH_LONG).show();
        }
        if (cursor != null) cursor.close();
    }
}
