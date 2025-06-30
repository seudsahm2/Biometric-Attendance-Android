package com.example.biometricattendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import org.json.JSONArray;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.UUID;
import java.util.concurrent.Executor;

public class EmployeeSignupActivity extends AppCompatActivity {
    private static final String TAG = "EmployeeSignup";
    private EditText etUsername, etPassword;
    private Button btnRegisterFingerprint, btnSignup;
    private ProgressBar progressBar;
    private boolean isFingerprintRegistered;
    private String fingerprintId;
    private String customFingerprintId;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_signup);

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnRegisterFingerprint = findViewById(R.id.btn_register_fingerprint);
        btnSignup = findViewById(R.id.btn_signup);
        progressBar = findViewById(R.id.progress_bar);
        prefs = getSharedPreferences("BiometricPrefs", MODE_PRIVATE);

        isFingerprintRegistered = false;
        fingerprintId = null;
        customFingerprintId = null;
        btnSignup.setEnabled(false);

        // Check if biometric hardware is available
        BiometricManager biometricManager = BiometricManager.from(this);
        int canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);
        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            String errorMsg = "Biometric hardware not available or not configured: " + canAuthenticate;
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            btnRegisterFingerprint.setEnabled(false);
            Log.e(TAG, errorMsg);
            return;
        }

        btnRegisterFingerprint.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            if (username.isEmpty()) {
                etUsername.setError("Username is required");
                etUsername.requestFocus();
                Log.w(TAG, "Username is empty");
                return;
            }

            // Launch fingerprint enrollment screen
            Intent enrollIntent = new Intent(Settings.ACTION_FINGERPRINT_ENROLL);
            if (enrollIntent.resolveActivity(getPackageManager()) == null) {
                enrollIntent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                Log.d(TAG, "Falling back to ACTION_SECURITY_SETTINGS");
            }
            startActivityForResult(enrollIntent, 1001);
            Toast.makeText(this, "Please add a new fingerprint in Settings, then return to verify", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Launched fingerprint enrollment for username: " + username);
        });

        btnSignup.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (validateInput(username, password)) {
                signupEmployee(username, password, fingerprintId, customFingerprintId);
                Log.d(TAG, "Signup button clicked for username: " + username);
            } else {
                Log.w(TAG, "Signup input validation failed");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            String username = etUsername.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(this, "Please enter a username first", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Username empty after returning from Settings");
                return;
            }
            fingerprintId = UUID.randomUUID().toString();
            customFingerprintId = UUID.randomUUID().toString(); // Use UUID for unique custom_fingerprint_id
            Log.d(TAG, "Generated fingerprint_id: " + fingerprintId + ", custom_fingerprint_id: " + customFingerprintId);
            verifyFingerprint();
        }
    }

    private void verifyFingerprint() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                String errorMsg = "Fingerprint verification error: " + errString;
                Toast.makeText(EmployeeSignupActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                isFingerprintRegistered = false;
                btnSignup.setEnabled(false);
                progressBar.setVisibility(View.GONE);
                btnRegisterFingerprint.setEnabled(true);
                Log.e(TAG, errorMsg);
                if (errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS) {
                    Toast.makeText(EmployeeSignupActivity.this, "No fingerprints enrolled. Please enroll a fingerprint in Settings.", Toast.LENGTH_LONG).show();
                } else if (errorCode == BiometricPrompt.ERROR_LOCKOUT) {
                    Toast.makeText(EmployeeSignupActivity.this, "Too many attempts. Try again later or check Settings.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Log.d(TAG, "Fingerprint verification succeeded");
                checkFingerprintUniqueness(fingerprintId);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(EmployeeSignupActivity.this, "Fingerprint verification failed", Toast.LENGTH_SHORT).show();
                isFingerprintRegistered = false;
                btnSignup.setEnabled(false);
                progressBar.setVisibility(View.GONE);
                btnRegisterFingerprint.setEnabled(true);
                Log.w(TAG, "Fingerprint verification failed");
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Verify New Fingerprint")
                .setSubtitle("Scan the newly registered fingerprint")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
        Log.d(TAG, "Initiated fingerprint verification");
    }

    private void checkFingerprintUniqueness(String newFingerprintId) {
        progressBar.setVisibility(View.VISIBLE);
        btnRegisterFingerprint.setEnabled(false);
        Log.d(TAG, "Checking fingerprint_id uniqueness: " + newFingerprintId);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        ApiModels.FingerprintRequest request = new ApiModels.FingerprintRequest(newFingerprintId);
        Log.d(TAG, "Sending fingerprint check request: " + request.fingerprint_id);
        Call<ApiModels.FingerprintResponse> call = apiService.checkFingerprint(request);

        call.enqueue(new Callback<ApiModels.FingerprintResponse>() {
            @Override
            public void onResponse(Call<ApiModels.FingerprintResponse> call, Response<ApiModels.FingerprintResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnRegisterFingerprint.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    ApiModels.FingerprintResponse resp = response.body();
                    Log.d(TAG, "Fingerprint check response: success=" + resp.isSuccess() + ", message=" + resp.getMessage());
                    if (resp.isSuccess()) {
                        Toast.makeText(EmployeeSignupActivity.this, "This fingerprint is already registered by another employee. Please use a different finger.", Toast.LENGTH_LONG).show();
                        isFingerprintRegistered = false;
                        btnSignup.setEnabled(false);
                        Log.w(TAG, "Fingerprint already registered in backend: " + newFingerprintId);
                    } else {
                        // Fingerprint is unique, proceed to save
                        saveFingerprintData(newFingerprintId, customFingerprintId);
                    }
                } else {
                    String errorMsg = "Fingerprint check failed: HTTP " + response.code() + ", " + response.message();
                    Toast.makeText(EmployeeSignupActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    isFingerprintRegistered = false;
                    btnSignup.setEnabled(false);
                    Log.e(TAG, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiModels.FingerprintResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnRegisterFingerprint.setEnabled(true);
                // Proceed if network failure, assuming fingerprint is unique
                saveFingerprintData(newFingerprintId, customFingerprintId);
                Log.w(TAG, "Fingerprint check failure, proceeding: " + t.getMessage());
            }
        });
    }

    private void saveFingerprintData(String fingerprintId, String customFingerprintId) {
        String username = etUsername.getText().toString().trim();
        try {
            String fingerprintData = prefs.getString("fingerprints", "[]");
            JSONArray fingerprints = new JSONArray(fingerprintData);
            JSONObject newFingerprint = new JSONObject();
            newFingerprint.put("username", username);
            newFingerprint.put("fingerprint_id", fingerprintId);
            newFingerprint.put("custom_fingerprint_id", customFingerprintId);
            fingerprints.put(newFingerprint);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("fingerprints", fingerprints.toString());
            editor.apply();
            Log.d(TAG, "Stored fingerprint in SharedPreferences: " + newFingerprint.toString());

            isFingerprintRegistered = true;
            btnSignup.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            btnRegisterFingerprint.setEnabled(true);
            Toast.makeText(EmployeeSignupActivity.this, "Fingerprint verified and ready for signup", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Fingerprint_id ready for signup: " + fingerprintId);
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            btnRegisterFingerprint.setEnabled(true);
            Toast.makeText(EmployeeSignupActivity.this, "Error storing fingerprint: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            isFingerprintRegistered = false;
            btnSignup.setEnabled(false);
            Log.e(TAG, "Error storing fingerprint in SharedPreferences: " + e.getMessage());
        }
    }

    private boolean validateInput(String username, String password) {
        if (username.isEmpty()) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            Log.w(TAG, "Validation failed: Username is empty");
            return false;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            Log.w(TAG, "Validation failed: Password is empty");
            return false;
        }
        if (!isFingerprintRegistered) {
            Toast.makeText(this, "Please verify your fingerprint", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Validation failed: Fingerprint not verified");
            return false;
        }
        return true;
    }

    private void signupEmployee(String username, String password, String fingerprintId, String customFingerprintId) {
        progressBar.setVisibility(View.VISIBLE);
        btnSignup.setEnabled(false);
        Log.d(TAG, "Attempting signup for username: " + username + ", fingerprint_id: " + fingerprintId + ", custom_fingerprint_id: " + customFingerprintId);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        ApiModels.EmployeeSignupRequest request = new ApiModels.EmployeeSignupRequest(username, password, fingerprintId, customFingerprintId);
        Log.d(TAG, "Signup request payload: " + request.toString());
        Call<ApiModels.SignupResponse> call = apiService.employeeSignup(request);

        call.enqueue(new Callback<ApiModels.SignupResponse>() {
            @Override
            public void onResponse(Call<ApiModels.SignupResponse> call, Response<ApiModels.SignupResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ApiModels.SignupResponse signupResponse = response.body();
                    Log.d(TAG, "Signup response: success=" + signupResponse.isSuccess() + ", message=" + signupResponse.getMessage());
                    if (signupResponse.isSuccess()) {
                        isFingerprintRegistered = false;
                        btnRegisterFingerprint.setEnabled(true);
                        Intent intent = new Intent(EmployeeSignupActivity.this, EmployeeLoginActivity.class);
                        startActivity(intent);
                        finish();
                        Toast.makeText(EmployeeSignupActivity.this, "Signup successful! Please login.", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "Signup successful for username: " + username);
                    } else {
                        Toast.makeText(EmployeeSignupActivity.this, signupResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        btnSignup.setEnabled(isFingerprintRegistered);
                        Log.e(TAG, "Signup failed: " + signupResponse.getMessage());
                    }
                } else {
                    String errorMsg = "Signup failed: HTTP " + response.code() + ", " + response.message();
                    Toast.makeText(EmployeeSignupActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    btnSignup.setEnabled(isFingerprintRegistered);
                    Log.e(TAG, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiModels.SignupResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSignup.setEnabled(isFingerprintRegistered);
                String errorMsg = "Signup error: " + t.getMessage();
                Toast.makeText(EmployeeSignupActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                Log.e(TAG, errorMsg);
            }
        });
    }
}