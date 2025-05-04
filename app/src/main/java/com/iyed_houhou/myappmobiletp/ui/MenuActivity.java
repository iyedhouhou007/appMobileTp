package com.iyed_houhou.myappmobiletp.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.iyed_houhou.myappmobiletp.R;

public class MenuActivity extends AppCompatActivity {
    private TextView tvWelcome;
    private Button btnProfile;
    private Button btnGrades;
    private Button btnAddGrades;
    private Button btnLogout;
    private String username;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Menu");
        }

        // Apply system spacing (status bar height) to the Toolbar
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), statusBarHeight, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        // Initialize views
        tvWelcome = findViewById(R.id.tvWelcome);
        btnProfile = findViewById(R.id.btnProfile);
        btnGrades = findViewById(R.id.btnGrades);
        btnAddGrades = findViewById(R.id.btnAddGrades);
        btnLogout = findViewById(R.id.btnLogout);

        // Get user info from intent
        username = getIntent().getStringExtra("username");
        userRole = getIntent().getStringExtra("userRole");

        // Set welcome message
        tvWelcome.setText("Welcome, " + username);

        // Show/hide buttons based on user role
        if ("Teacher".equals(userRole)) {
            btnProfile.setVisibility(View.GONE);
            btnGrades.setVisibility(View.GONE);
            btnAddGrades.setVisibility(View.VISIBLE);
        } else {
            btnAddGrades.setVisibility(View.GONE);
        }

        // Set up click listeners
        setupClickListeners();
    }

    private void setupClickListeners() {
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, StudentProfileActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        btnGrades.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, GradesActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        btnAddGrades.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, AddGradesActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            // Clear login session
            SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            // Navigate to login screen
            Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}