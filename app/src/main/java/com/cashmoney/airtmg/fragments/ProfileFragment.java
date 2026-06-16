package com.cashmoney.airtmg.fragments;

import android.content.Intent;
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
import com.cashmoney.airtmg.LoginActivity;
import com.cashmoney.airtmg.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvName = view.findViewById(R.id.tvProfileName);
        tvEmail = view.findViewById(R.id.tvProfileEmail);
        TextView tvLogoutBtn = view.findViewById(R.id.tvLogout);
        dbHelper = new DatabaseHelper(getContext());

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String email = firebaseUser.getEmail();
            loadProfileFromLocal(email);
        }

        tvLogoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        return view;
    }

    private void loadProfileFromLocal(String email) {
        if (email == null) return;
        Cursor cursor = dbHelper.getUserDataByEmail(email);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                tvName.setText(cursor.getString(cursor.getColumnIndexOrThrow("username")));
                tvEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            }
            cursor.close();
        }
    }
}
