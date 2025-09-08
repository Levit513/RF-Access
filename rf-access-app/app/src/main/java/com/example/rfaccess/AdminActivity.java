package com.example.rfaccess;

import android.app.Activity;
import android.content.SharedPreferences;
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
    private EditText usernameInput;
    private EditText cardDataInput;
    private Button sendButton;
    private Button logoutButton;
    private TextView welcomeText;
    private RecyclerView usersRecyclerView;
    private RequestQueue requestQueue;

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
        usersRecyclerView = findViewById(R.id.usersRecyclerView);

        // Initialize network queue
        requestQueue = Volley.newRequestQueue(this);

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

        // Load registered users
        loadRegisteredUsers();
    }

    private void sendProgrammingData() {
        String username = usernameInput.getText().toString().trim();
        String cardData = cardDataInput.getText().toString().trim();

        if (username.isEmpty() || cardData.isEmpty()) {
            Toast.makeText(this, "Please enter both username and card data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Simulate sending push notification with programming data
        simulatePushNotification(username, cardData);
        
        // Clear inputs
        usernameInput.setText("");
        cardDataInput.setText("");
        
        Toast.makeText(this, "Programming data sent to " + username, Toast.LENGTH_SHORT).show();
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
