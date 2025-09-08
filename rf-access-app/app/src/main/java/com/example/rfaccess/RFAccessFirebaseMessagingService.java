package com.example.rfaccess;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.SharedPreferences;
import android.content.Intent;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import androidx.core.app.NotificationCompat;
import org.json.JSONObject;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class RFAccessFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "RFAccessFCM";
    private static final String CHANNEL_ID = "rf_access_notifications";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            
            String type = remoteMessage.getData().get("type");
            if ("programming_data".equals(type)) {
                handleProgrammingData(remoteMessage.getData());
            } else if ("emulation_control".equals(type)) {
                handleEmulationControl(remoteMessage.getData());
            }
        }

        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            showProgrammingNotification(
                remoteMessage.getNotification().getTitle(),
                remoteMessage.getNotification().getBody()
            );
        }
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        
        // Send token to your server or store it locally
        SharedPreferences prefs = getSharedPreferences("RFAccessPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("fcm_token", token);
        editor.apply();
        
        // In a real implementation, you would send this token to your backend
        sendRegistrationToServer(token);
    }

    private void storePendingCardData(String username, String cardData) {
        SharedPreferences prefs = getSharedPreferences("RFAccessPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("pending_card_data", cardData);
        editor.putString("pending_username", username);
        editor.putLong("pending_timestamp", System.currentTimeMillis());
        editor.apply();
        
        Log.d(TAG, "Programming data stored for user: " + username);
    }

    private void handleProgrammingData(Map<String, String> data) {
        String username = data.get("username");
        String cardData = data.get("cardData");
        storePendingCardData(username, cardData);
        showProgrammingNotification(username, cardData);
    }

    private void handleEmulationControl(Map<String, String> data) {
        try {
            String payloadJson = data.get("payload");
            if (payloadJson != null) {
                JSONObject payload = new JSONObject(payloadJson);
                String username = payload.getString("username");
                String cardData = payload.getString("cardData");
                boolean activate = payload.getBoolean("activate");
                
                // Update emulation settings
                MifareEmulationService.updateEmulationData(getApplicationContext(), cardData, activate);
                
                // Show notification
                String title = activate ? "Card Emulation Activated" : "Card Emulation Deactivated";
                String message = activate ? "Your phone will now emulate a MIFARE card" : "Card emulation has been disabled";
                showEmulationNotification(title, message);
                
                Log.d(TAG, "Emulation control updated for user: " + username + ", active: " + activate);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling emulation control", e);
        }
    }

    private void showProgrammingNotification(String username, String cardData) {
        String title = "RF Access Programming Ready";
        String body = "New card programming data received. Tap to open RF Access.";
        
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("from_notification", true);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            getApplicationContext(), 
            0, 
            intent, 
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder notificationBuilder =
            new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    private void showNotification(String title, String body) {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            getApplicationContext(), 
            0, 
            intent, 
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder notificationBuilder =
            new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(1, notificationBuilder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "RF Access Notifications";
            String description = "Notifications for RF Access card programming";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendRegistrationToServer(String token) {
        // In a real implementation, send the token to your backend server
        // This would typically be done via an HTTP request to your API
        Log.d(TAG, "Token would be sent to server: " + token);
    }
}
