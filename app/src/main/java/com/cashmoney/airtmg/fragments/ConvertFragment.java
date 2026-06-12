package com.cashmoney.airtmg.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cashmoney.airtmg.R;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ConvertFragment extends Fragment {

    private Spinner spinnerFrom, spinnerTo;
    private EditText etFromAmount;
    private TextView tvToAmount, tvExchangeRate;
    private Button btnConvert;
    private ImageButton btnSwap;

    private static final Map<String, Double> RATES = new HashMap<>();

    static {
        // Rates relative to 1 USD
        RATES.put("USD", 1.0);
        RATES.put("EUR", 0.92);
        RATES.put("GBP", 0.79);
        RATES.put("JPY", 151.62);
        RATES.put("CAD", 1.35);
        RATES.put("AUD", 1.52);
        RATES.put("KES", 132.50);
        RATES.put("NGN", 1450.00);
        RATES.put("ZAR", 18.85);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_convert, container, false);

        spinnerFrom = view.findViewById(R.id.spinnerFrom);
        spinnerTo = view.findViewById(R.id.spinnerTo);
        etFromAmount = view.findViewById(R.id.etFromAmount);
        tvToAmount = view.findViewById(R.id.tvToAmount);
        tvExchangeRate = view.findViewById(R.id.tvExchangeRate);
        btnConvert = view.findViewById(R.id.btnConvert);
        btnSwap = view.findViewById(R.id.btnSwap);

        String[] currencies = RATES.keySet().toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, currencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);

        // Set defaults
        spinnerFrom.setSelection(adapter.getPosition("USD"));
        spinnerTo.setSelection(adapter.getPosition("EUR"));

        btnConvert.setOnClickListener(v -> performConversion());

        btnSwap.setOnClickListener(v -> {
            int fromPos = spinnerFrom.getSelectedItemPosition();
            spinnerFrom.setSelection(spinnerTo.getSelectedItemPosition());
            spinnerTo.setSelection(fromPos);
            performConversion();
        });

        etFromAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performConversion();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void performConversion() {
        String fromCurrency = spinnerFrom.getSelectedItem().toString();
        String toCurrency = spinnerTo.getSelectedItem().toString();
        String amountStr = etFromAmount.getText().toString();

        if (amountStr.isEmpty()) {
            tvToAmount.setText("0.00");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            double fromRate = RATES.get(fromCurrency);
            double toRate = RATES.get(toCurrency);

            // Convert to USD first, then to target currency
            double amountInUsd = amount / fromRate;
            double result = amountInUsd * toRate;

            tvToAmount.setText(String.format(Locale.getDefault(), "%.2f", result));
            
            double rate = toRate / fromRate;
            tvExchangeRate.setText(String.format(Locale.getDefault(), "1 %s = %.4f %s", fromCurrency, rate, toCurrency));
            
        } catch (NumberFormatException e) {
            tvToAmount.setText("0.00");
        }
    }
}
