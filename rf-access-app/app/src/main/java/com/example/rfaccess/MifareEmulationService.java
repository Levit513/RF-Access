package com.example.rfaccess;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;
import android.content.SharedPreferences;
import android.content.Intent;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import androidx.core.app.NotificationCompat;

public class MifareEmulationService extends HostApduService {
    private static final String TAG = "MifareEmulation";
    private static final String PREFS_NAME = "RFAccessPrefs";
    private static final String CHANNEL_ID = "rf_access_emulation";
    
    // MIFARE Classic commands
    private static final byte[] SELECT_APDU = {(byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00};
    private static final byte[] READ_BINARY = {(byte) 0x00, (byte) 0xB0};
    private static final byte[] UPDATE_BINARY = {(byte) 0x00, (byte) 0xD6};
    
    // Response codes
    private static final byte[] SUCCESS_RESPONSE = {(byte) 0x90, (byte) 0x00};
    private static final byte[] ERROR_RESPONSE = {(byte) 0x6F, (byte) 0x00};
    private static final byte[] NOT_FOUND_RESPONSE = {(byte) 0x6A, (byte) 0x82};
    
    private byte[] emulatedCardData;
    private boolean isEmulationActive = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "MIFARE Emulation Service created");
        createNotificationChannel();
        loadEmulationData();
    }
    
    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        Log.d(TAG, "Received APDU: " + bytesToHex(commandApdu));
        
        if (!isEmulationActive) {
            Log.d(TAG, "Emulation not active, ignoring APDU");
            return ERROR_RESPONSE;
        }
        
        // Handle SELECT command
        if (isSelectCommand(commandApdu)) {
            Log.d(TAG, "SELECT command received");
            showEmulationNotification("Card Reader Detected", "RF Access card is being read");
            return SUCCESS_RESPONSE;
        }
        
        // Handle READ BINARY command
        if (isReadCommand(commandApdu)) {
            Log.d(TAG, "READ command received");
            return handleReadCommand(commandApdu);
        }
        
        // Handle UPDATE BINARY command
        if (isUpdateCommand(commandApdu)) {
            Log.d(TAG, "UPDATE command received");
            return handleUpdateCommand(commandApdu);
        }
        
        Log.d(TAG, "Unknown command, returning error");
        return ERROR_RESPONSE;
    }
    
    @Override
    public void onDeactivated(int reason) {
        Log.d(TAG, "Service deactivated, reason: " + reason);
        hideEmulationNotification();
    }
    
    private boolean isSelectCommand(byte[] apdu) {
        return apdu.length >= 4 && 
               apdu[0] == SELECT_APDU[0] && 
               apdu[1] == SELECT_APDU[1] && 
               apdu[2] == SELECT_APDU[2] && 
               apdu[3] == SELECT_APDU[3];
    }
    
    private boolean isReadCommand(byte[] apdu) {
        return apdu.length >= 2 && 
               apdu[0] == READ_BINARY[0] && 
               apdu[1] == READ_BINARY[1];
    }
    
    private boolean isUpdateCommand(byte[] apdu) {
        return apdu.length >= 2 && 
               apdu[0] == UPDATE_BINARY[0] && 
               apdu[1] == UPDATE_BINARY[1];
    }
    
    private byte[] handleReadCommand(byte[] apdu) {
        if (emulatedCardData == null) {
            Log.d(TAG, "No card data to emulate");
            return NOT_FOUND_RESPONSE;
        }
        
        // Extract offset and length from APDU
        int offset = 0;
        int length = 16; // Default MIFARE block size
        
        if (apdu.length >= 4) {
            offset = ((apdu[2] & 0xFF) << 8) | (apdu[3] & 0xFF);
        }
        if (apdu.length >= 5) {
            length = apdu[4] & 0xFF;
        }
        
        // Return requested data
        byte[] responseData = new byte[length + 2];
        if (offset < emulatedCardData.length) {
            int copyLength = Math.min(length, emulatedCardData.length - offset);
            System.arraycopy(emulatedCardData, offset, responseData, 0, copyLength);
        }
        
        // Add success response code
        responseData[length] = SUCCESS_RESPONSE[0];
        responseData[length + 1] = SUCCESS_RESPONSE[1];
        
        Log.d(TAG, "Returning card data: " + bytesToHex(responseData));
        return responseData;
    }
    
    private byte[] handleUpdateCommand(byte[] apdu) {
        Log.d(TAG, "UPDATE command - emulated card is read-only");
        showEmulationNotification("Write Attempt", "Reader tried to write to RF Access card");
        return ERROR_RESPONSE; // Emulated card is read-only
    }
    
    private void loadEmulationData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String emulationDataHex = prefs.getString("emulationData", null);
        isEmulationActive = prefs.getBoolean("emulationActive", false);
        
        if (emulationDataHex != null) {
            emulatedCardData = hexToBytes(emulationDataHex);
            Log.d(TAG, "Loaded emulation data: " + emulationDataHex);
        } else {
            // Default MIFARE Classic data
            emulatedCardData = createDefaultCardData();
            Log.d(TAG, "Using default card data");
        }
    }
    
    private byte[] createDefaultCardData() {
        // Create a basic MIFARE Classic 1K structure
        byte[] defaultData = new byte[1024]; // 1K card
        
        // Block 0 (UID block) - example data
        defaultData[0] = (byte) 0x12; // UID byte 1
        defaultData[1] = (byte) 0x34; // UID byte 2
        defaultData[2] = (byte) 0x56; // UID byte 3
        defaultData[3] = (byte) 0x78; // UID byte 4
        defaultData[4] = (byte) 0x9A; // BCC
        
        // Add RF Access identifier in block 1
        String rfAccessId = "RF ACCESS CARD";
        byte[] idBytes = rfAccessId.getBytes();
        System.arraycopy(idBytes, 0, defaultData, 16, Math.min(idBytes.length, 16));
        
        return defaultData;
    }
    
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
            CHANNEL_ID,
            "RF Access Emulation",
            NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Notifications for RF Access card emulation");
        
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
    
    private void showEmulationNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true);
        
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(1001, builder.build());
    }
    
    private void hideEmulationNotification() {
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.cancel(1001);
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }
    
    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
    
    // Public method to update emulation data remotely
    public static void updateEmulationData(android.content.Context context, String hexData, boolean activate) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("emulationData", hexData);
        editor.putBoolean("emulationActive", activate);
        editor.apply();
        
        Log.d(TAG, "Emulation data updated remotely");
    }
}
