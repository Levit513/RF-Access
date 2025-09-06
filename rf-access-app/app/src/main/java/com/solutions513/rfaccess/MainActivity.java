package com.solutions513.rfaccess;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "RFAccess";
    
    private NfcAdapter nfcAdapter;
    private TextView statusText;
    private TextView instructionText;
    private Button programButton;
    private RequestQueue requestQueue;
    
    // Admin portal URL - update this with your Railway deployment URL
    private static final String ADMIN_PORTAL_URL = "https://rf-access-admin-production.up.railway.app/program";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize views
        statusText = findViewById(R.id.statusText);
        instructionText = findViewById(R.id.instructionText);
        programButton = findViewById(R.id.programButton);
        
        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this);
        
        // Initialize NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        
        if (nfcAdapter == null) {
            statusText.setText("NFC not supported on this device");
            programButton.setEnabled(false);
            return;
        }
        
        if (!nfcAdapter.isEnabled()) {
            statusText.setText("Please enable NFC in Settings");
            programButton.setEnabled(false);
            return;
        }
        
        statusText.setText("Ready to program RF Access Card");
        
        // Set up program button
        programButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchProgrammingData();
            }
        });
        
        // Handle NFC intent if app was launched by NFC
        handleIntent(getIntent());
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }
    
    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) ||
            NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) ||
            NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                fetchProgrammingDataAndWrite(tag);
            }
        }
    }
    
    private void fetchProgrammingData() {
        statusText.setText("Fetching programming data...");
        
        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.GET,
            ADMIN_PORTAL_URL,
            null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    statusText.setText("Programming data received. Hold card to phone to program.");
                    instructionText.setText("Hold your RF Access Card to the back of your phone");
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    statusText.setText("Error fetching programming data");
                    Log.e(TAG, "Network error: " + error.getMessage());
                    Toast.makeText(MainActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        );
        
        requestQueue.add(request);
    }
    
    private void fetchProgrammingDataAndWrite(Tag tag) {
        statusText.setText("Programming card...");
        
        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.GET,
            ADMIN_PORTAL_URL,
            null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        // Parse the programming data
                        JSONObject sectors = response.getJSONObject("sectors");
                        programMifareCard(tag, sectors);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing programming data", e);
                        statusText.setText("Error: Invalid programming data format");
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    statusText.setText("Error fetching programming data");
                    Log.e(TAG, "Network error: " + error.getMessage());
                }
            }
        );
        
        requestQueue.add(request);
    }
    
    private void programMifareCard(Tag tag, JSONObject sectors) {
        MifareClassic mifare = MifareClassic.get(tag);
        if (mifare == null) {
            statusText.setText("Error: Not a MIFARE Classic card");
            return;
        }
        
        try {
            mifare.connect();
            
            // Iterate through sectors and program them
            for (int sector = 0; sector < mifare.getSectorCount(); sector++) {
                String sectorKey = String.valueOf(sector);
                if (sectors.has(sectorKey)) {
                    try {
                        JSONObject sectorData = sectors.getJSONObject(sectorKey);
                        
                        // Authenticate with default key (you may need to customize this)
                        byte[] key = MifareClassic.KEY_DEFAULT;
                        if (!mifare.authenticateSectorWithKeyA(sector, key)) {
                            Log.w(TAG, "Authentication failed for sector " + sector);
                            continue;
                        }
                        
                        // Write blocks in this sector (except trailer block)
                        int firstBlock = mifare.sectorToBlock(sector);
                        int blockCount = mifare.getBlockCountInSector(sector);
                        
                        for (int i = 0; i < blockCount - 1; i++) { // -1 to skip trailer block
                            int blockIndex = firstBlock + i;
                            String blockKey = String.valueOf(i);
                            
                            if (sectorData.has(blockKey)) {
                                String hexData = sectorData.getString(blockKey);
                                byte[] blockData = hexStringToByteArray(hexData);
                                
                                if (blockData.length == 16) {
                                    mifare.writeBlock(blockIndex, blockData);
                                    Log.d(TAG, "Written block " + blockIndex + " in sector " + sector);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing sector " + sector + " data", e);
                    }
                }
            }
            
            mifare.close();
            statusText.setText("✓ RF Access Card programmed successfully!");
            instructionText.setText("Your card is ready to use");
            
        } catch (IOException e) {
            Log.e(TAG, "Error programming MIFARE card", e);
            statusText.setText("Error programming card");
            try {
                mifare.close();
            } catch (IOException ignored) {}
        }
    }
    
    private byte[] hexStringToByteArray(String hex) {
        // Remove any spaces or non-hex characters
        hex = hex.replaceAll("[^0-9A-Fa-f]", "");
        
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            // Enable foreground dispatch for NFC
            Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
                this, 0, intent, android.app.PendingIntent.FLAG_MUTABLE);
            
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }
}
