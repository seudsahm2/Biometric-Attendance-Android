package com.example.biometricattendance;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET; // Added
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @POST("admin/login/")
    Call<ApiModels.LoginResponse> adminLogin(@Body ApiModels.LoginRequest request);

    @POST("employee/login/")
    Call<ApiModels.LoginResponse> employeeLogin(@Body ApiModels.LoginRequest request);

    @POST("admin/signup/")
    Call<ApiModels.SignupResponse> adminSignup(@Body ApiModels.SignupRequest request);

    @POST("employee/signup/")
    Call<ApiModels.SignupResponse> employeeSignup(@Body ApiModels.EmployeeSignupRequest request);

    @POST("fingerprint/validate/")
    Call<ApiModels.FingerprintResponse> validateFingerprint(@Body ApiModels.FingerprintRequest request);

    @POST("api/fingerprint/check/")
    Call<ApiModels.FingerprintResponse> checkFingerprint(@Body ApiModels.FingerprintRequest request);

    @POST("api/fingerprint/try-all/")
    Call<ApiModels.FingerprintResponse> tryAllFingerprints();

    @GET("attendance/list/") // Changed to GET
    Call<List<ApiModels.AttendanceResponse>> getAttendanceList();

    @GET("attendance/count/")
    Call<ApiModels.AttendanceCountResponse> getAttendanceCount(@Query("username") String username);
}


