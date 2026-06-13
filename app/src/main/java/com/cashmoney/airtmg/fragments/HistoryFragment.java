package com.cashmoney.airtmg.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cashmoney.airtmg.DatabaseHelper;
import com.cashmoney.airtmg.R;

import java.util.Locale;
import java.util.Objects;

public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private DatabaseHelper dbHelper;
    private EditText etSearch;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        rvHistory = view.findViewById(R.id.rvHistory);
        etSearch = view.findViewById(R.id.etSearch);
        
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        dbHelper = new DatabaseHelper(getContext());

        String username = null;
        if (getActivity() != null && getActivity().getIntent() != null) {
            username = getActivity().getIntent().getStringExtra("USERNAME");
        }
        if (username == null) username = "DemoUser";

        Cursor userCursor = dbHelper.getUserData(username);
        if (userCursor != null && userCursor.moveToFirst()) {
            userId = userCursor.getInt(userCursor.getColumnIndexOrThrow("id"));
            userCursor.close();
            loadHistory("");
        }

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadHistory(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void loadHistory(String query) {
        Cursor transCursor;
        if (query.isEmpty()) {
            transCursor = dbHelper.getTransactions(userId);
        } else {
            transCursor = dbHelper.searchTransactions(userId, query);
        }
        
        if (transCursor != null) {
            rvHistory.setAdapter(new TransactionAdapter(transCursor));
        }
    }

    private static class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
        private final Cursor cursor;

        public TransactionAdapter(Cursor cursor) {
            this.cursor = cursor;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (cursor.moveToPosition(position)) {
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));

                holder.tvType.setText(type);
                holder.tvDesc.setText(String.format("%s • %s", desc, timestamp));
                holder.tvAmount.setText(String.format(Locale.getDefault(), "$%.2f", amount));
                
                if (Objects.equals(type, "SEND")) {
                    holder.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark));
                } else {
                    holder.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_dark));
                }
            }
        }

        @Override
        public int getItemCount() {
            return cursor != null ? cursor.getCount() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvType, tvDesc, tvAmount;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvType = itemView.findViewById(R.id.tvTransType);
                tvDesc = itemView.findViewById(R.id.tvTransDesc);
                tvAmount = itemView.findViewById(R.id.tvTransAmount);
            }
        }
    }
}
