package com.example.biometricattendance;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Redirect to Admin or Employee Login/Signup or Fingerprint Scan
        findViewById(R.id.btn_admin_login).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AdminLoginActivity.class));
        });

        findViewById(R.id.btn_admin_signup).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AdminSignupActivity.class));
        });

        findViewById(R.id.btn_employee_login).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, EmployeeLoginActivity.class));
        });

        findViewById(R.id.btn_employee_signup).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, EmployeeSignupActivity.class));
        });

        findViewById(R.id.btn_fingerprint_scan).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, FingerprintScanActivity.class));
        });
    }
}