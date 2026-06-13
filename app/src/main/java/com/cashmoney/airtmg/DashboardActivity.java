package com.cashmoney.airtmg;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.cashmoney.airtmg.fragments.ConvertFragment;
import com.cashmoney.airtmg.fragments.HomeFragment;
import com.cashmoney.airtmg.fragments.HistoryFragment;
import com.cashmoney.airtmg.fragments.ProfileFragment;
import com.cashmoney.airtmg.fragments.ReceiveFragment;
import com.cashmoney.airtmg.fragments.SendFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_send) {
                selectedFragment = new SendFragment();
            } else if (itemId == R.id.nav_receive) {
                selectedFragment = new ReceiveFragment();
            } else if (itemId == R.id.nav_convert) {
                selectedFragment = new ConvertFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            } else {
                return false;
            }

            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
            return true;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

    public void navigateTo(int navItemId) {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(navItemId);
    }

    public void showHistory() {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, new HistoryFragment())
                .addToBackStack(null)
                .commit();
    }
}
