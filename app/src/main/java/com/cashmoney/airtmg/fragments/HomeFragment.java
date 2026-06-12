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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cashmoney.airtmg.DatabaseHelper;
import com.cashmoney.airtmg.R;

import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvUsername, tvBalance, tvIncome, tvExpense;
    private RecyclerView rvRecent;
    private SwipeRefreshLayout swipeRefresh;
    private DatabaseHelper dbHelper;
    private String username;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvUsername = view.findViewById(R.id.tvUsername);
        tvBalance = view.findViewById(R.id.tvBalance);
        tvIncome = view.findViewById(R.id.tvIncome);
        tvExpense = view.findViewById(R.id.tvExpense);
        rvRecent = view.findViewById(R.id.rvRecentHistory);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        dbHelper = new DatabaseHelper(getContext());
        rvRecent.setLayoutManager(new LinearLayoutManager(getContext()));

        username = getActivity().getIntent().getStringExtra("USERNAME");
        if (username == null) username = "DemoUser";

        tvUsername.setText(username);
        
        loadDashboardData();

        swipeRefresh.setOnRefreshListener(this::loadDashboardData);

        return view;
    }

    private void loadDashboardData() {
        Cursor cursor = dbHelper.getUserData(username);
        if (cursor.moveToFirst()) {
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            double balance = cursor.getDouble(cursor.getColumnIndexOrThrow("balance"));
            tvBalance.setText(String.format(Locale.getDefault(), "$%.2f", balance));

            // Load transactions for stats and list
            Cursor transCursor = dbHelper.getTransactions(userId);
            updateStats(transCursor);
            
            // Limit to 3 recent transactions for the home screen
            rvRecent.setAdapter(new RecentAdapter(transCursor));
        }
        cursor.close();
        swipeRefresh.setRefreshing(false);
    }

    private void updateStats(Cursor cursor) {
        double income = 0;
        double expense = 0;
        if (cursor.moveToFirst()) {
            do {
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                if (type.equals("RECEIVE")) {
                    income += amount;
                } else if (type.equals("SEND")) {
                    expense += amount;
                }
            } while (cursor.moveToNext());
        }
        tvIncome.setText(String.format(Locale.getDefault(), "+$%.2f", income));
        tvExpense.setText(String.format(Locale.getDefault(), "-$%.2f", expense));
    }

    private class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.ViewHolder> {
        private Cursor cursor;

        public RecentAdapter(Cursor cursor) {
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
                holder.tvAmount.setText(String.format(Locale.getDefault(), "$%.2f", amount));

                if (type.equals("SEND")) {
                    holder.tvAmount.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                } else {
                    holder.tvAmount.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                }
            }
        }

        @Override
        public int getItemCount() {
            return Math.min(cursor.getCount(), 3); // Show only top 3
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
