package com.cashmoney.airtmg;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.cashmoney.airtmg.fragments.ConvertFragment;
import com.cashmoney.airtmg.fragments.HomeFragment;
import com.cashmoney.airtmg.fragments.HistoryFragment;
import com.cashmoney.airtmg.fragments.SendFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        // Load the default fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;

                int itemId = item.getItemId();
                if (itemId == R.id.nav_dashboard) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.nav_send) {
                    selectedFragment = new SendFragment();
                } else if (itemId == R.id.nav_convert) {
                    selectedFragment = new ConvertFragment();
                } else if (itemId == R.id.nav_history) {
                    selectedFragment = new HistoryFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                }

                return true;
            };
}
