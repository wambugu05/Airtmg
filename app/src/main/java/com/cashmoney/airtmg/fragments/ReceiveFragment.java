package com.cashmoney.airtmg.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cashmoney.airtmg.R;

public class ReceiveFragment extends Fragment {

    private TextView tvUserHandle;
    private Button btnShare;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receive, container, false);

        tvUserHandle = view.findViewById(R.id.tvUserHandle);
        btnShare = view.findViewById(R.id.btnShareDetails);

        String username = getActivity().getIntent().getStringExtra("USERNAME");
        if (username == null) username = "DemoUser";

        tvUserHandle.setText("@" + username);

        btnShare.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "Send me money on Airtmg! My username is: @" + tvUserHandle.getText().toString());
            startActivity(Intent.createChooser(intent, "Share via"));
        });

        return view;
    }
}
