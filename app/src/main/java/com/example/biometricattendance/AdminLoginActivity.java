package com.example.biometricattendance;

import android.content.Intent;
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

public class AdminLoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin, btnSignup;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        // Initialize UI components
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnSignup = findViewById(R.id.btn_signup);
        progressBar = findViewById(R.id.progress_bar);

        // Set click listener for login button
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (validateInput(username, password)) {
                loginAdmin(username, password);
            }
        });

        // Set click listener for signup button
        btnSignup.setOnClickListener(v -> {
            startActivity(new Intent(AdminLoginActivity.this, AdminSignupActivity.class));
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

    private void loginAdmin(String username, String password) {
        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        // Create API request
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        ApiModels.LoginRequest request = new ApiModels.LoginRequest(username, password);
        Call<ApiModels.LoginResponse> call = apiService.adminLogin(request);

        call.enqueue(new Callback<ApiModels.LoginResponse>() {
            @Override
            public void onResponse(Call<ApiModels.LoginResponse> call, Response<ApiModels.LoginResponse> response) {
                // Hide loading indicator
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    ApiModels.LoginResponse loginResponse = response.body();
                    if (loginResponse.isSuccess()) {
                        // Clear input fields
                        etUsername.setText("");
                        etPassword.setText("");
                        // Navigate to Admin Homepage
                        startActivity(new Intent(AdminLoginActivity.this, AdminHomepageActivity.class));
                        finish(); // Prevent going back to login screen
                    } else {
                        Toast.makeText(AdminLoginActivity.this, loginResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AdminLoginActivity.this, "Login failed: Invalid response from server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiModels.LoginResponse> call, Throwable t) {
                // Hide loading indicator
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Toast.makeText(AdminLoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}