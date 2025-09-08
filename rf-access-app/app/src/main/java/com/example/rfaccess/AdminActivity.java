package com.example.rfaccess;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

public class AdminActivity extends Activity {
    private static final String PREFS_NAME = "RFAccessPrefs";
    
    private EditText usernameInput;
    private EditText cardDataInput;
    private Button sendButton;
    private Button logoutButton;
    private Button viewDistributionsButton;
    private Button deleteDistributionButton;
    private Button reissueDistributionButton;
    private TextView welcomeText;
    private TextView distributionStatusText;
    private RecyclerView usersRecyclerView;
    private RequestQueue requestQueue;
    private DistributionManager distributionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize views
        welcomeText = findViewById(R.id.welcomeText);
        usernameInput = findViewById(R.id.usernameInput);
        cardDataInput = findViewById(R.id.cardDataInput);
        sendButton = findViewById(R.id.sendButton);
        logoutButton = findViewById(R.id.logoutButton);
        viewDistributionsButton = findViewById(R.id.viewDistributionsButton);
        deleteDistributionButton = findViewById(R.id.deleteDistributionButton);
        reissueDistributionButton = findViewById(R.id.reissueDistributionButton);
        distributionStatusText = findViewById(R.id.distributionStatusText);
        usersRecyclerView = findViewById(R.id.usersRecyclerView);

        // Initialize network queue and distribution manager
        requestQueue = Volley.newRequestQueue(this);
        distributionManager = new DistributionManager(this);

        // Set up RecyclerView
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set welcome message
        SharedPreferences prefs = getSharedPreferences("RFAccessPrefs", MODE_PRIVATE);
        String adminUsername = prefs.getString("username", "Admin");
        welcomeText.setText("Welcome, " + adminUsername + "!");

        // Set up button listeners
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendProgrammingData();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        viewDistributionsButton.setOnClickListener(v -> viewActiveDistributions());
        deleteDistributionButton.setOnClickListener(v -> deleteOldDistributions());
        reissueDistributionButton.setOnClickListener(v -> showReissueDialog());

        // Add emulation control button
        Button emulationControlButton = findViewById(R.id.emulationControlButton);
        emulationControlButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, EmulationControlActivity.class);
            startActivity(intent);
        });

        // Load registered users and update distribution status
        loadRegisteredUsers();
        updateDistributionStatus();
    }

    private void sendProgrammingData() {
        String username = usernameInput.getText().toString().trim();
        String cardData = cardDataInput.getText().toString().trim();

        if (username.isEmpty() || cardData.isEmpty()) {
            Toast.makeText(this, "Please enter both username and card data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create distribution with deep link
        DistributionManager.Distribution distribution = distributionManager.createDistribution(username, cardData);
        
        // Simulate sending push notification with programming data
        simulatePushNotification(username, cardData);
        
        // Show deep link to admin
        showDistributionLink(distribution);
        
        // Clear inputs
        usernameInput.setText("");
        cardDataInput.setText("");
        
        // Update distribution status
        updateDistributionStatus();
        
        Toast.makeText(this, "Distribution created for " + username, Toast.LENGTH_SHORT).show();
    }

    private void simulatePushNotification(String username, String cardData) {
        // In a real implementation, this would send via Firebase Cloud Messaging
        // For now, we'll simulate by creating a notification payload
        
        try {
            JSONObject payload = new JSONObject();
            payload.put("username", username);
            payload.put("cardData", cardData);
            payload.put("action", "program");
            payload.put("timestamp", System.currentTimeMillis());
            
            // Log the simulated notification
            android.util.Log.d("AdminActivity", "Simulated push notification: " + payload.toString());
            
            // In production, you would use Firebase Admin SDK or your backend API
            // to send the actual push notification to the user's device
            
        } catch (Exception e) {
            android.util.Log.e("AdminActivity", "Error creating notification payload", e);
        }
    }

    private void showDistributionLink(DistributionManager.Distribution distribution) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Distribution Created");
        builder.setMessage("Deep Link for " + distribution.username + ":\n\n" + distribution.deepLink);
        builder.setPositiveButton("Copy Link", (dialog, which) -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("RF Access Link", distribution.deepLink);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Link copied to clipboard", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Close", null);
        builder.show();
    }

    private void updateDistributionStatus() {
        int activeCount = distributionManager.getActiveDistributionCount();
        if (activeCount == 0) {
            distributionStatusText.setText("No active distributions");
        } else {
            distributionStatusText.setText(activeCount + " active distribution" + (activeCount == 1 ? "" : "s"));
        }
    }

    private void viewActiveDistributions() {
        java.util.List<DistributionManager.Distribution> distributions = distributionManager.getActiveDistributions();
        
        if (distributions.isEmpty()) {
            Toast.makeText(this, "No active distributions", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder message = new StringBuilder("Active Distributions:\n\n");
        for (DistributionManager.Distribution dist : distributions) {
            message.append("• ").append(dist.username)
                   .append(" (").append(dist.getFormattedTimestamp()).append(")\n")
                   .append("  Link: ").append(dist.deepLink).append("\n\n");
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Active Distributions");
        builder.setMessage(message.toString());
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    private void deleteOldDistributions() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Delete Old Distributions");
        builder.setMessage("Delete distributions older than:");
        
        String[] options = {"1 hour", "1 day", "1 week", "1 month", "All distributions"};
        long[] timeValues = {
            60 * 60 * 1000L,        // 1 hour
            24 * 60 * 60 * 1000L,   // 1 day
            7 * 24 * 60 * 60 * 1000L, // 1 week
            30 * 24 * 60 * 60 * 1000L, // 1 month
            Long.MAX_VALUE          // All
        };
        
        builder.setItems(options, (dialog, which) -> {
            int deletedCount;
            if (which == 4) { // All distributions
                distributionManager.clearAllDistributions();
                deletedCount = distributionManager.getActiveDistributionCount();
            } else {
                deletedCount = distributionManager.deleteOldDistributions(timeValues[which]);
            }
            
            updateDistributionStatus();
            Toast.makeText(this, "Deleted " + deletedCount + " distribution(s)", Toast.LENGTH_SHORT).show();
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showReissueDialog() {
        java.util.List<DistributionManager.Distribution> distributions = distributionManager.getActiveDistributions();
        
        if (distributions.isEmpty()) {
            Toast.makeText(this, "No active distributions to reissue", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] usernames = new String[distributions.size()];
        for (int i = 0; i < distributions.size(); i++) {
            usernames[i] = distributions.get(i).username + " (" + distributions.get(i).getFormattedTimestamp() + ")";
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Reissue Distribution");
        builder.setMessage("Select distribution to reissue with new link:");
        
        builder.setItems(usernames, (dialog, which) -> {
            DistributionManager.Distribution selectedDist = distributions.get(which);
            DistributionManager.Distribution newDist = distributionManager.reissueDistribution(selectedDist.id);
            
            if (newDist != null) {
                showDistributionLink(newDist);
                updateDistributionStatus();
                Toast.makeText(this, "New distribution created for " + newDist.username, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to reissue distribution", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void logout() {
        // Clear login session
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().clear().apply();
        
        // Return to login
        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadRegisteredUsers() {
        // Simulate loading registered users
        // In a real implementation, this would fetch from your backend API
        
        // For demo purposes, show some sample users
        String[] sampleUsers = {"john_doe", "jane_smith", "mike_wilson", "sarah_jones"};
        
        // You would typically use a RecyclerView adapter here
        // For now, we'll just log the users
        for (String user : sampleUsers) {
            android.util.Log.d("AdminActivity", "Registered user: " + user);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
    }
}
