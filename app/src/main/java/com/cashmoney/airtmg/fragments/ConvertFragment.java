package com.cashmoney.airtmg.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cashmoney.airtmg.R;

import java.util.Locale;

public class ConvertFragment extends Fragment {

    private EditText etUsdAmount;
    private TextView tvResult;
    private Button btnConvert;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_convert, container, false);

        etUsdAmount = view.findViewById(R.id.etUsdAmount);
        tvResult = view.findViewById(R.id.tvConvertedAmount);
        btnConvert = view.findViewById(R.id.btnConvert);

        btnConvert.setOnClickListener(v -> {
            String usdStr = etUsdAmount.getText().toString();
            if (!usdStr.isEmpty()) {
                double usd = Double.parseDouble(usdStr);
                double eur = usd * 0.92; // Simple fixed rate
                tvResult.setText(String.format(Locale.getDefault(), "Result: %.2f EUR", eur));
            }
        });

        return view;
    }
}
