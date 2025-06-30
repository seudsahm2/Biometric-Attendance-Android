package com.example.biometricattendance;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import org.json.JSONArray;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.concurrent.Executor;

public class FingerprintScanActivity extends AppCompatActivity {
    private static final String TAG = "FingerprintScan";
    private TextView tvStatus;
    private Button btnScan;
    private Spinner spinnerUsername;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private SharedPreferences prefs;
    private ArrayList<String> usernames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint_scan);

        tvStatus = findViewById(R.id.tv_status);
        btnScan = findViewById(R.id.btn_scan);
        spinnerUsername = findViewById(R.id.spinner_username);
        prefs = getSharedPreferences("BiometricPrefs", MODE_PRIVATE);
        usernames = new ArrayList<>();

        // Populate username spinner
        populateUsernameSpinner();

        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                tvStatus.setText("Authentication error: " + errString);
                Toast.makeText(FingerprintScanActivity.this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Authentication error: " + errString);
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                tvStatus.setText("Fingerprint scan successful, validating...");
                Log.d(TAG, "Fingerprint scan successful");
                validateFingerprint();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                tvStatus.setText("Authentication failed");
                Toast.makeText(FingerprintScanActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Authentication failed");
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Authentication")
                .setSubtitle("Scan your fingerprint to mark attendance")
                .setNegativeButtonText("Cancel")
                .build();

        btnScan.setOnClickListener(v -> {
            if (spinnerUsername.getSelectedItem() == null) {
                Toast.makeText(FingerprintScanActivity.this, "Please select a username", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "No username selected");
                return;
            }
            biometricPrompt.authenticate(promptInfo);
            Log.d(TAG, "Scan button clicked, initiating fingerprint authentication");
        });
    }

    private void populateUsernameSpinner() {
        String fingerprintData = prefs.getString("fingerprints", "[]");
        try {
            JSONArray fingerprints = new JSONArray(fingerprintData);
            usernames.clear();
            for (int i = 0; i < fingerprints.length(); i++) {
                JSONObject fingerprint = fingerprints.getJSONObject(i);
                String username = fingerprint.getString("username");
                usernames.add(username);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, usernames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerUsername.setAdapter(adapter);
            Log.d(TAG, "Populated username spinner with " + usernames.size() + " users");
        } catch (Exception e) {
            Log.e(TAG, "Error populating username spinner: " + e.getMessage());
            Toast.makeText(this, "Error loading usernames", Toast.LENGTH_SHORT).show();
        }
    }

    private void validateFingerprint() {
        String selectedUsername = (String) spinnerUsername.getSelectedItem();
        if (selectedUsername == null) {
            tvStatus.setText("Error: No username selected");
            Toast.makeText(this, "Please select a username", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "No username selected for validation");
            return;
        }

        String fingerprintData = prefs.getString("fingerprints", "[]");
        try {
            JSONArray fingerprints = new JSONArray(fingerprintData);
            String fingerprintId = null;
            for (int i = 0; i < fingerprints.length(); i++) {
                JSONObject fingerprint = fingerprints.getJSONObject(i);
                if (fingerprint.getString("username").equals(selectedUsername)) {
                    fingerprintId = fingerprint.getString("fingerprint_id");
                    break;
                }
            }
            if (fingerprintId == null) {
                tvStatus.setText("Error: Fingerprint not found for user");
                Toast.makeText(this, "Fingerprint not found for " + selectedUsername, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "No fingerprint_id found for username: " + selectedUsername);
                return;
            }

            Log.d(TAG, "Validating fingerprint_id: " + fingerprintId + " for username: " + selectedUsername);
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            ApiModels.FingerprintRequest request = new ApiModels.FingerprintRequest(fingerprintId);
            Call<ApiModels.FingerprintResponse> call = apiService.validateFingerprint(request);

            call.enqueue(new Callback<ApiModels.FingerprintResponse>() {
                @Override
                public void onResponse(Call<ApiModels.FingerprintResponse> call, Response<ApiModels.FingerprintResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        tvStatus.setText(response.body().getMessage());
                        Toast.makeText(FingerprintScanActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        if (response.body().isSuccess()) {
                            Log.i(TAG, "Attendance marked: " + response.body().getMessage());
                        } else {
                            Log.w(TAG, "Validation failed: " + response.body().getMessage());
                        }
                    } else {
                        String errorMsg = "Error: Invalid response from server (HTTP " + response.code() + ")";
                        tvStatus.setText(errorMsg);
                        Toast.makeText(FingerprintScanActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, errorMsg);
                    }
                }

                @Override
                public void onFailure(Call<ApiModels.FingerprintResponse> call, Throwable t) {
                    String errorMsg = "Error: " + t.getMessage();
                    tvStatus.setText(errorMsg);
                    Toast.makeText(FingerprintScanActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Validation error: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            tvStatus.setText("Error: Failed to read fingerprints");
            Toast.makeText(this, "Error: Failed to read fingerprints", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error parsing fingerprints: " + e.getMessage());
        }
    }
}