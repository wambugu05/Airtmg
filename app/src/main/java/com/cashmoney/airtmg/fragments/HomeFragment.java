package com.cashmoney.airtmg.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cashmoney.airtmg.DashboardActivity;
import com.cashmoney.airtmg.DatabaseHelper;
import com.cashmoney.airtmg.R;

import java.util.Locale;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private TextView tvBalance, tvIncome, tvExpense;
    private RecyclerView rvRecent;
    private SwipeRefreshLayout swipeRefresh;
    private DatabaseHelper dbHelper;
    private String username;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView tvUsernameDisplay = view.findViewById(R.id.tvUsername);
        tvBalance = view.findViewById(R.id.tvBalance);
        tvIncome = view.findViewById(R.id.tvIncome);
        tvExpense = view.findViewById(R.id.tvExpense);
        rvRecent = view.findViewById(R.id.rvRecentHistory);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        TextView tvViewAll = view.findViewById(R.id.tvViewAll);

        dbHelper = new DatabaseHelper(getContext());
        rvRecent.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getActivity() != null && getActivity().getIntent() != null) {
            username = getActivity().getIntent().getStringExtra("USERNAME");
        }
        if (username == null) username = "DemoUser";

        tvUsernameDisplay.setText(username);
        
        loadDashboardData();

        swipeRefresh.setOnRefreshListener(this::loadDashboardData);

        tvViewAll.setOnClickListener(v -> {
            if (getActivity() instanceof DashboardActivity) {
                ((DashboardActivity) getActivity()).showHistory();
            }
        });

        view.findViewById(R.id.actionSend).setOnClickListener(v -> {
            if (getActivity() instanceof DashboardActivity) {
                ((DashboardActivity) getActivity()).navigateTo(R.id.nav_send);
            }
        });

        view.findViewById(R.id.actionReceive).setOnClickListener(v -> {
            if (getActivity() instanceof DashboardActivity) {
                ((DashboardActivity) getActivity()).navigateTo(R.id.nav_receive);
            }
        });

        view.findViewById(R.id.actionConvert).setOnClickListener(v -> {
            if (getActivity() instanceof DashboardActivity) {
                ((DashboardActivity) getActivity()).navigateTo(R.id.nav_convert);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void loadDashboardData() {
        Cursor cursor = dbHelper.getUserData(username);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                double balance = cursor.getDouble(cursor.getColumnIndexOrThrow("balance"));
                tvBalance.setText(String.format(Locale.getDefault(), "$%.2f", balance));

                Cursor transCursor = dbHelper.getTransactions(userId);
                if (transCursor != null) {
                    updateStats(transCursor);
                    rvRecent.setAdapter(new RecentAdapter(transCursor));
                }
            }
            cursor.close();
        }
        swipeRefresh.setRefreshing(false);
    }

    private void updateStats(Cursor cursor) {
        double incomeTotal = 0;
        double expenseTotal = 0;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                if (Objects.equals(type, "RECEIVE")) {
                    incomeTotal += amount;
                } else if (Objects.equals(type, "SEND")) {
                    expenseTotal += amount;
                }
            } while (cursor.moveToNext());
        }
        tvIncome.setText(String.format(Locale.getDefault(), "+$%.2f", incomeTotal));
        tvExpense.setText(String.format(Locale.getDefault(), "-$%.2f", expenseTotal));
    }

    private static class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.ViewHolder> {
        private final Cursor cursor;

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
            return cursor != null ? Math.min(cursor.getCount(), 3) : 0;
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
