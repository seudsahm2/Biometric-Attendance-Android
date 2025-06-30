package com.example.biometricattendance;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminHomepageActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvNoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_homepage);

        // Initialize UI components
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        tvNoData = findViewById(R.id.tv_no_data);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fetchAttendanceList();
    }

    private void fetchAttendanceList() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvNoData.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<ApiModels.AttendanceResponse>> call = apiService.getAttendanceList();

        call.enqueue(new Callback<List<ApiModels.AttendanceResponse>>() {
            @Override
            public void onResponse(Call<List<ApiModels.AttendanceResponse>> call, Response<List<ApiModels.AttendanceResponse>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<ApiModels.AttendanceResponse> attendanceList = response.body();
                    if (attendanceList.isEmpty()) {
                        tvNoData.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        AttendanceAdapter adapter = new AttendanceAdapter(attendanceList);
                        recyclerView.setAdapter(adapter);
                    }
                } else {
                    tvNoData.setVisibility(View.VISIBLE);
                    Toast.makeText(AdminHomepageActivity.this, "Failed to load attendance data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ApiModels.AttendanceResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvNoData.setVisibility(View.VISIBLE);
                Toast.makeText(AdminHomepageActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}