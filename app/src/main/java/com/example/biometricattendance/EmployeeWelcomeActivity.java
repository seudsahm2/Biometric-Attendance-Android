package com.example.biometricattendance;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeWelcomeActivity extends AppCompatActivity {
    private TextView tvWelcome, tvAttendanceCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_welcome);

        tvWelcome = findViewById(R.id.tv_welcome);
        tvAttendanceCount = findViewById(R.id.tv_attendance_count);

        String username = getIntent().getStringExtra("username");
        if (username == null) {
            tvWelcome.setText("Welcome, Employee!");
            tvAttendanceCount.setText("Error: Username not provided");
            return;
        }

        tvWelcome.setText("Welcome, " + username + "!");
        fetchAttendanceCount(username);
    }

    private void fetchAttendanceCount(String username) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ApiModels.AttendanceCountResponse> call = apiService.getAttendanceCount(username);

        call.enqueue(new Callback<ApiModels.AttendanceCountResponse>() {
            @Override
            public void onResponse(Call<ApiModels.AttendanceCountResponse> call, Response<ApiModels.AttendanceCountResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    int count = response.body().getAttendanceCount();
                    tvAttendanceCount.setText("Days Attended: " + count);
                } else {
                    tvAttendanceCount.setText("Error: Unable to fetch attendance count");
                }
            }

            @Override
            public void onFailure(Call<ApiModels.AttendanceCountResponse> call, Throwable t) {
                tvAttendanceCount.setText("Error: " + t.getMessage());
            }
        });
    }
}