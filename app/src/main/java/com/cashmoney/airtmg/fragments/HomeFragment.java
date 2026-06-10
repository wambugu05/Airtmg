package com.cashmoney.airtmg.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cashmoney.airtmg.DatabaseHelper;
import com.cashmoney.airtmg.R;

public class HomeFragment extends Fragment {

    private TextView tvUsername, tvBalance;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvUsername = view.findViewById(R.id.tvUsername);
        tvBalance = view.findViewById(R.id.tvBalance);
        dbHelper = new DatabaseHelper(getContext());

        // For demo purposes, we'll use a default user if not passed
        String username = getActivity().getIntent().getStringExtra("USERNAME");
        if (username == null) username = "DemoUser";

        tvUsername.setText(username);
        loadBalance(username);

        return view;
    }

    private void loadBalance(String username) {
        Cursor cursor = dbHelper.getUserData(username);
        if (cursor.moveToFirst()) {
            double balance = cursor.getDouble(cursor.getColumnIndexOrThrow("balance"));
            tvBalance.setText(String.format("$%.2f", balance));
        }
        cursor.close();
    }
}
