package com.example.biometricattendance;

import com.google.gson.annotations.SerializedName;

public class ApiModels {

    public static class LoginRequest {
        public String username;
        public String password;

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    public static class LoginResponse {
        private boolean success;
        private String message;
        private String username;

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getUsername() {
            return username;
        }
    }

    public static class SignupRequest {
        @SerializedName("username")
        private String username;

        @SerializedName("password")
        private String password;

        public SignupRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }

    public static class FingerprintRequest {
        public String fingerprint_id;

        public FingerprintRequest(String fingerprint_id) {
            this.fingerprint_id = fingerprint_id;
        }
    }

    public static class FingerprintResponse {
        private boolean success;
        private String message;

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class EmployeeSignupRequest {
        public String username;
        public String password;
        public String fingerprint_id;
        public String custom_fingerprint_id;

        public EmployeeSignupRequest(String username, String password, String fingerprint_id, String custom_fingerprint_id) {
            this.username = username;
            this.password = password;
            this.fingerprint_id = fingerprint_id;
            this.custom_fingerprint_id = custom_fingerprint_id;
        }
    }

    public static class SignupResponse {
        private boolean success;
        private String message;

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }



    public static class AttendanceResponse {
        @SerializedName("employee_username")
        private String employeeUsername;

        @SerializedName("timestamp")
        private String timestamp;

        public String getEmployeeUsername() {
            return employeeUsername;
        }

        public String getTimestamp() {
            return timestamp;
        }
    }

    public static class AttendanceCountResponse {
        private boolean success;
        private String message;
        private int attendance_count;

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public int getAttendanceCount() {
            return attendance_count;
        }
    }
}
