package com.example.rfaccess;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;
import org.json.JSONException;

public class EmulationControlActivity extends AppCompatActivity {
    private static final String TAG = "EmulationControl";
    private static final String PREFS_NAME = "RFAccessPrefs";
    
    private EditText cardDataInput;
    private Switch emulationToggle;
    private TextView statusText;
    private Button updateButton;
    private Button sendRemoteButton;
    private EditText targetUsernameInput;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emulation_control);
        
        initializeViews();
        loadCurrentSettings();
        setupClickListeners();
    }
    
    private void initializeViews() {
        cardDataInput = findViewById(R.id.cardDataInput);
        emulationToggle = findViewById(R.id.emulationToggle);
        statusText = findViewById(R.id.statusText);
        updateButton = findViewById(R.id.updateButton);
        sendRemoteButton = findViewById(R.id.sendRemoteButton);
        targetUsernameInput = findViewById(R.id.targetUsernameInput);
    }
    
    private void loadCurrentSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String currentData = prefs.getString("emulationData", "");
        boolean isActive = prefs.getBoolean("emulationActive", false);
        
        cardDataInput.setText(currentData);
        emulationToggle.setChecked(isActive);
        updateStatusText(isActive);
    }
    
    private void setupClickListeners() {
        updateButton.setOnClickListener(v -> updateLocalEmulation());
        sendRemoteButton.setOnClickListener(v -> sendRemoteEmulation());
        emulationToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateStatusText(isChecked);
        });
    }
    
    private void updateLocalEmulation() {
        String cardData = cardDataInput.getText().toString().trim();
        boolean isActive = emulationToggle.isChecked();
        
        if (cardData.isEmpty()) {
            Toast.makeText(this, "Please enter card data", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validate hex data
        if (!isValidHex(cardData)) {
            Toast.makeText(this, "Invalid hex data format", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Update local emulation
        MifareEmulationService.updateEmulationData(this, cardData, isActive);
        
        Toast.makeText(this, "Local emulation updated", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Local emulation updated - Active: " + isActive);
    }
    
    private void sendRemoteEmulation() {
        String targetUsername = targetUsernameInput.getText().toString().trim();
        String cardData = cardDataInput.getText().toString().trim();
        boolean isActive = emulationToggle.isChecked();
        
        if (targetUsername.isEmpty()) {
            Toast.makeText(this, "Please enter target username", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (cardData.isEmpty()) {
            Toast.makeText(this, "Please enter card data", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!isValidHex(cardData)) {
            Toast.makeText(this, "Invalid hex data format", Toast.LENGTH_SHORT).show();
            return;
        }
        
        sendEmulationCommand(targetUsername, cardData, isActive);
    }
    
    private void sendEmulationCommand(String username, String cardData, boolean activate) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("type", "emulation_control");
            payload.put("username", username);
            payload.put("cardData", cardData);
            payload.put("activate", activate);
            payload.put("timestamp", System.currentTimeMillis());
            
            // Send via Firebase Cloud Messaging
            sendFCMMessage(payload);
            
        } catch (JSONException e) {
            Log.e(TAG, "Error creating emulation payload", e);
            Toast.makeText(this, "Error creating command", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void sendFCMMessage(JSONObject payload) {
        RequestQueue queue = Volley.newRequestQueue(this);
        
        try {
            JSONObject fcmMessage = new JSONObject();
            JSONObject data = new JSONObject();
            data.put("type", "emulation_control");
            data.put("payload", payload.toString());
            
            fcmMessage.put("data", data);
            fcmMessage.put("to", "/topics/user_" + payload.getString("username"));
            
            JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                "https://fcm.googleapis.com/fcm/send",
                fcmMessage,
                response -> {
                    Toast.makeText(this, "Remote emulation command sent", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "FCM message sent successfully");
                },
                error -> {
                    Toast.makeText(this, "Failed to send remote command", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "FCM send error", error);
                }
            ) {
                @Override
                public java.util.Map<String, String> getHeaders() {
                    java.util.Map<String, String> headers = new java.util.HashMap<>();
                    headers.put("Authorization", "key=YOUR_SERVER_KEY"); // Replace with actual key
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };
            
            queue.add(request);
            
        } catch (JSONException e) {
            Log.e(TAG, "Error creating FCM message", e);
            Toast.makeText(this, "Error sending command", Toast.LENGTH_SHORT).show();
        }
    }
    
    private boolean isValidHex(String hex) {
        if (hex.length() % 2 != 0) return false;
        return hex.matches("[0-9A-Fa-f]+");
    }
    
    private void updateStatusText(boolean isActive) {
        if (isActive) {
            statusText.setText("Status: Emulation ACTIVE - Card will respond to readers");
            statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            statusText.setText("Status: Emulation INACTIVE - Card will not respond");
            statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }
}
