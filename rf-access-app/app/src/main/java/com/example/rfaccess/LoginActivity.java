package com.example.rfaccess;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "RFAccess";
    private static final String PREFS_NAME = "RFAccessPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_IS_ADMIN = "isAdmin";

    private TextInputEditText usernameInput;
    private Button signInButton;
    private Button adminButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        handleDeepLink();
        checkExistingLogin();
        setupClickListeners();
    }

    private void initializeViews() {
        usernameInput = findViewById(R.id.usernameInput);
        signInButton = findViewById(R.id.signInButton);
        adminButton = findViewById(R.id.adminButton);
    }

    private void handleDeepLink() {
        Intent intent = getIntent();
        Uri data = intent.getData();
        
        if (data != null) {
            Log.d(TAG, "Deep link received: " + data.toString());
            
            // Extract parameters from deep link
            String username = data.getQueryParameter("username");
            String cardData = data.getQueryParameter("cardData");
            String action = data.getQueryParameter("action");
            
            if (username != null && !username.isEmpty()) {
                usernameInput.setText(username);
                Log.d(TAG, "Username from deep link: " + username);
            }
            
            if (cardData != null && !cardData.isEmpty()) {
                // Store card data for programming
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                prefs.edit()
                    .putString("pendingCardData", cardData)
                    .putString("deepLinkAction", action != null ? action : "program")
                    .apply();
                Log.d(TAG, "Card data stored from deep link");
                
                Toast.makeText(this, "Card programming data received", Toast.LENGTH_SHORT).show();
            }
            
            // Show welcome message for deep link users
            Toast.makeText(this, "Welcome to RF Access!", Toast.LENGTH_LONG).show();
        }
    }

    private void checkExistingLogin() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        
        if (isLoggedIn) {
            String username = prefs.getString(KEY_USERNAME, "");
            boolean isAdmin = prefs.getBoolean(KEY_IS_ADMIN, false);
            
            Log.d(TAG, "User already logged in: " + username + " (Admin: " + isAdmin + ")");
            
            if (isAdmin) {
                startActivity(new Intent(this, AdminActivity.class));
            } else {
                startActivity(new Intent(this, MainActivity.class));
            }
            finish();
        }
    }

    private void setupClickListeners() {
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleUserLogin();
            }
        });

        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAdminLogin();
            }
        });
    }

    private void handleUserLogin() {
        String username = usernameInput.getText().toString().trim();
        
        if (username.length() < 3) {
            Toast.makeText(this, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save login state
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
            .putString(KEY_USERNAME, username)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putBoolean(KEY_IS_ADMIN, false)
            .apply();

        Log.d(TAG, "User login successful: " + username);
        Toast.makeText(this, "Welcome, " + username + "!", Toast.LENGTH_SHORT).show();

        // Navigate to main activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void handleAdminLogin() {
        String username = usernameInput.getText().toString().trim().toLowerCase();
        
        if (!isAdminUsername(username)) {
            Toast.makeText(this, "Invalid admin credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save admin login state
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
            .putString(KEY_USERNAME, username)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putBoolean(KEY_IS_ADMIN, true)
            .apply();

        Log.d(TAG, "Admin login successful: " + username);
        Toast.makeText(this, "Admin access granted", Toast.LENGTH_SHORT).show();

        // Navigate to admin activity
        Intent intent = new Intent(this, AdminActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isAdminUsername(String username) {
        return username.equals("admin") || username.equals("administrator");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink();
    }
}
