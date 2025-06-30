package com.example.biometricattendance;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminSignupActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnSignup;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_signup);

        // Initialize UI components
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnSignup = findViewById(R.id.btn_signup);
        progressBar = findViewById(R.id.progress_bar);

        // Set click listener for signup button
        btnSignup.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (validateInput(username, password)) {
                signupAdmin(username, password);
            }
        });
    }

    private boolean validateInput(String username, String password) {
        if (username.isEmpty()) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return false;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void signupAdmin(String username, String password) {
        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);
        btnSignup.setEnabled(false);

        // Create API request
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        ApiModels.SignupRequest request = new ApiModels.SignupRequest(username, password);
        Call<ApiModels.SignupResponse> call = apiService.adminSignup(request);

        call.enqueue(new Callback<ApiModels.SignupResponse>() {
            @Override
            public void onResponse(Call<ApiModels.SignupResponse> call, Response<ApiModels.SignupResponse> response) {
                // Hide loading indicator
                progressBar.setVisibility(View.GONE);
                btnSignup.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    ApiModels.SignupResponse signupResponse = response.body();
                    if (signupResponse.isSuccess()) {
                        // Clear input fields
                        etUsername.setText("");
                        etPassword.setText("");
                        Toast.makeText(AdminSignupActivity.this, "Signup successful! Please login.", Toast.LENGTH_SHORT).show();
                        finish(); // Return to login screen
                    } else {
                        Toast.makeText(AdminSignupActivity.this, signupResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AdminSignupActivity.this, "Signup failed: Invalid response from server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiModels.SignupResponse> call, Throwable t) {
                // Hide loading indicator
                progressBar.setVisibility(View.GONE);
                btnSignup.setEnabled(true);
                Toast.makeText(AdminSignupActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}