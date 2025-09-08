package com.example.rfaccess;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DistributionManager {
    private static final String TAG = "DistributionManager";
    private static final String PREFS_NAME = "RFAccessDistributions";
    private static final String DISTRIBUTIONS_KEY = "active_distributions";
    
    private Context context;
    private SharedPreferences prefs;
    
    public DistributionManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public static class Distribution {
        public String id;
        public String username;
        public String cardData;
        public String deepLink;
        public long timestamp;
        public boolean isActive;
        
        public Distribution(String username, String cardData) {
            this.id = UUID.randomUUID().toString();
            this.username = username;
            this.cardData = cardData;
            this.timestamp = System.currentTimeMillis();
            this.isActive = true;
            this.deepLink = generateDeepLink(username, cardData);
        }
        
        public Distribution(JSONObject json) throws JSONException {
            this.id = json.getString("id");
            this.username = json.getString("username");
            this.cardData = json.getString("cardData");
            this.deepLink = json.getString("deepLink");
            this.timestamp = json.getLong("timestamp");
            this.isActive = json.getBoolean("isActive");
        }
        
        public JSONObject toJSON() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("username", username);
            json.put("cardData", cardData);
            json.put("deepLink", deepLink);
            json.put("timestamp", timestamp);
            json.put("isActive", isActive);
            return json;
        }
        
        private String generateDeepLink(String username, String cardData) {
            return "rfaccess://open?username=" + username + 
                   "&cardData=" + cardData + 
                   "&action=program&id=" + id;
        }
        
        public String reissueWithNewLink() {
            this.id = UUID.randomUUID().toString();
            this.timestamp = System.currentTimeMillis();
            this.deepLink = generateDeepLink(username, cardData);
            return this.deepLink;
        }
        
        public String getFormattedTimestamp() {
            return new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", 
                   java.util.Locale.getDefault()).format(new java.util.Date(timestamp));
        }
    }
    
    public Distribution createDistribution(String username, String cardData) {
        Distribution distribution = new Distribution(username, cardData);
        saveDistribution(distribution);
        Log.d(TAG, "Created distribution for user: " + username);
        return distribution;
    }
    
    public List<Distribution> getActiveDistributions() {
        List<Distribution> distributions = new ArrayList<>();
        try {
            String distributionsJson = prefs.getString(DISTRIBUTIONS_KEY, "[]");
            JSONArray array = new JSONArray(distributionsJson);
            
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Distribution dist = new Distribution(obj);
                if (dist.isActive) {
                    distributions.add(dist);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error loading distributions", e);
        }
        return distributions;
    }
    
    public List<Distribution> getAllDistributions() {
        List<Distribution> distributions = new ArrayList<>();
        try {
            String distributionsJson = prefs.getString(DISTRIBUTIONS_KEY, "[]");
            JSONArray array = new JSONArray(distributionsJson);
            
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                distributions.add(new Distribution(obj));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error loading all distributions", e);
        }
        return distributions;
    }
    
    public void saveDistribution(Distribution distribution) {
        try {
            List<Distribution> distributions = getAllDistributions();
            
            // Remove existing distribution with same ID
            distributions.removeIf(d -> d.id.equals(distribution.id));
            
            // Add updated distribution
            distributions.add(distribution);
            
            // Save to preferences
            JSONArray array = new JSONArray();
            for (Distribution dist : distributions) {
                array.put(dist.toJSON());
            }
            
            prefs.edit().putString(DISTRIBUTIONS_KEY, array.toString()).apply();
            Log.d(TAG, "Distribution saved: " + distribution.id);
            
        } catch (JSONException e) {
            Log.e(TAG, "Error saving distribution", e);
        }
    }
    
    public boolean deleteDistribution(String distributionId) {
        try {
            List<Distribution> distributions = getAllDistributions();
            boolean removed = distributions.removeIf(d -> d.id.equals(distributionId));
            
            if (removed) {
                JSONArray array = new JSONArray();
                for (Distribution dist : distributions) {
                    array.put(dist.toJSON());
                }
                prefs.edit().putString(DISTRIBUTIONS_KEY, array.toString()).apply();
                Log.d(TAG, "Distribution deleted: " + distributionId);
            }
            
            return removed;
        } catch (JSONException e) {
            Log.e(TAG, "Error deleting distribution", e);
            return false;
        }
    }
    
    public int deleteOldDistributions(long olderThanMillis) {
        try {
            List<Distribution> distributions = getAllDistributions();
            long cutoffTime = System.currentTimeMillis() - olderThanMillis;
            
            int deletedCount = 0;
            distributions.removeIf(d -> {
                if (d.timestamp < cutoffTime) {
                    deletedCount++;
                    return true;
                }
                return false;
            });
            
            JSONArray array = new JSONArray();
            for (Distribution dist : distributions) {
                array.put(dist.toJSON());
            }
            prefs.edit().putString(DISTRIBUTIONS_KEY, array.toString()).apply();
            
            Log.d(TAG, "Deleted " + deletedCount + " old distributions");
            return deletedCount;
            
        } catch (JSONException e) {
            Log.e(TAG, "Error deleting old distributions", e);
            return 0;
        }
    }
    
    public Distribution reissueDistribution(String distributionId) {
        try {
            List<Distribution> distributions = getAllDistributions();
            
            for (Distribution dist : distributions) {
                if (dist.id.equals(distributionId)) {
                    // Create new distribution with same data but new ID and link
                    Distribution newDist = new Distribution(dist.username, dist.cardData);
                    
                    // Deactivate old distribution
                    dist.isActive = false;
                    
                    // Save both distributions
                    saveDistribution(dist);
                    saveDistribution(newDist);
                    
                    Log.d(TAG, "Reissued distribution: " + distributionId + " -> " + newDist.id);
                    return newDist;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reissuing distribution", e);
        }
        return null;
    }
    
    public Distribution findDistributionByUsername(String username) {
        List<Distribution> distributions = getActiveDistributions();
        for (Distribution dist : distributions) {
            if (dist.username.equals(username)) {
                return dist;
            }
        }
        return null;
    }
    
    public int getActiveDistributionCount() {
        return getActiveDistributions().size();
    }
    
    public void clearAllDistributions() {
        prefs.edit().remove(DISTRIBUTIONS_KEY).apply();
        Log.d(TAG, "All distributions cleared");
    }
}
