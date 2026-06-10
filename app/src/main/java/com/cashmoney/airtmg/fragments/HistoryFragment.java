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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cashmoney.airtmg.DatabaseHelper;
import com.cashmoney.airtmg.R;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        rvHistory = view.findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        dbHelper = new DatabaseHelper(getContext());

        loadHistory();

        return view;
    }

    private void loadHistory() {
        String username = getActivity().getIntent().getStringExtra("USERNAME");
        if (username == null) username = "DemoUser";

        Cursor userCursor = dbHelper.getUserData(username);
        if (userCursor.moveToFirst()) {
            int userId = userCursor.getInt(userCursor.getColumnIndexOrThrow("id"));
            Cursor transCursor = dbHelper.getTransactions(userId);
            rvHistory.setAdapter(new TransactionAdapter(transCursor));
        }
        userCursor.close();
    }

    private class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
        private Cursor cursor;

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

                holder.tvType.setText(type);
                holder.tvDesc.setText(desc);
                holder.tvAmount.setText(String.format("$%.2f", amount));
                
                if (type.equals("SEND")) {
                    holder.tvAmount.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                } else {
                    holder.tvAmount.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                }
            }
        }

        @Override
        public int getItemCount() {
            return cursor.getCount();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
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
