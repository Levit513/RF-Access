package com.example.rfaccess;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "RFAccess";
    private static final String PREFS_NAME = "RFAccessPrefs";
    
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFilters;
    private String[][] techLists;
    
    private TextView statusText;
    private TextView instructionText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        initializeNFC();
        checkForPendingCardData();
        updateUI();
    }

    private void initializeViews() {
        statusText = findViewById(R.id.statusText);
        instructionText = findViewById(R.id.instructionText);
        // cardStatusText is not needed as it doesn't exist in the layout
    }

    private void initializeNFC() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        
        if (nfcAdapter == null) {
            statusText.setText("RF Access - NFC Not Available");
            instructionText.setText("This device does not support NFC functionality.");
            Log.e(TAG, "NFC not supported on this device");
            return;
        }
        
        if (!nfcAdapter.isEnabled()) {
            statusText.setText("RF Access - Enable NFC");
            instructionText.setText("Please enable NFC in your device settings to use RF Access.");
            Log.w(TAG, "NFC is disabled");
            return;
        }
        
        setupNfcIntent();
        Log.d(TAG, "NFC initialized successfully");
    }

    private void setupNfcIntent() {
        pendingIntent = PendingIntent.getActivity(
            this, 0, 
            new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 
            PendingIntent.FLAG_MUTABLE
        );
        
        IntentFilter techDiscovered = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        intentFilters = new IntentFilter[]{techDiscovered};
        
        techLists = new String[][]{
            new String[]{MifareClassic.class.getName()}
        };
    }

    private void checkForPendingCardData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String pendingCardData = prefs.getString("pendingCardData", null);
        String deepLinkAction = prefs.getString("deepLinkAction", null);
        
        if (pendingCardData != null) {
            Log.d(TAG, "Found pending card data from deep link");
            instructionText.setText("Card programming data ready! Hold your RF Access card near the phone to program it.");
            
            Toast.makeText(this, "Ready to program your card with new data", Toast.LENGTH_LONG).show();
        }
    }

    private void updateUI() {
        if (nfcAdapter == null || !nfcAdapter.isEnabled()) {
            return;
        }
        
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String username = prefs.getString("username", "User");
        String pendingCardData = prefs.getString("pendingCardData", null);
        
        statusText.setText("RF Access - Ready");
        
        if (pendingCardData != null) {
            instructionText.setText("Hold your RF Access card near the phone to program it with new data.");
        } else {
            instructionText.setText("Hold your RF Access card near the phone to program it.");
        }
        
        Log.d(TAG, "UI updated for user: " + username);
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }
    
    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                handleNfcTag(tag);
            }
        }
    }

    private void handleNfcTag(Tag tag) {
        Log.d(TAG, "NFC tag detected: " + tag.toString());
        
        // Update UI to show card detected
        instructionText.setText("Programming your RF Access card...");
        
        // Check for pending card data from deep link
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String pendingCardData = prefs.getString("pendingCardData", null);
        String deepLinkAction = prefs.getString("deepLinkAction", "program");
        
        // Simulate card programming
        simulateCardProgramming(tag, pendingCardData, deepLinkAction);
        
        // Clear pending data after programming
        if (pendingCardData != null) {
            prefs.edit()
                .remove("pendingCardData")
                .remove("deepLinkAction")
                .apply();
            Log.d(TAG, "Cleared pending card data after programming");
        }
    }

    private void simulateCardProgramming(Tag tag, String cardData, String action) {
        try {
            // Simulate programming delay
            Thread.sleep(2000);
            
            runOnUiThread(() -> {
                if (cardData != null) {
                    // Programming with specific data from deep link
                    instructionText.setText("Your RF Access card has been programmed successfully with the new data!");
                    Toast.makeText(this, "Card programmed with new access data", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Card programmed with deep link data: " + action);
                } else {
                    // Standard programming
                    instructionText.setText("Your RF Access card has been programmed successfully!");
                    Toast.makeText(this, "RF Access card programming complete", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Standard card programming completed");
                }
                
                // Reset UI after a delay
                new android.os.Handler().postDelayed(() -> {
                    updateUI();
                }, 3000);
            });
            
        } catch (InterruptedException e) {
            Log.e(TAG, "Programming simulation interrupted", e);
            runOnUiThread(() -> {
                instructionText.setText("Card programming failed. Please try again.");
                Toast.makeText(this, "Programming failed. Please try again.", Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, techLists);
            Log.d(TAG, "NFC foreground dispatch enabled");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
            Log.d(TAG, "NFC foreground dispatch disabled");
        }
    }
}
