package com.example.save.utils;

import android.content.Context;
import android.widget.Toast;

import com.example.save.data.network.ApiResponse;

import retrofit2.Response;

public class ApiErrorHandler {

    public static void handleError(Context context, Throwable throwable) {
        String errorMessage = "An error occurred. Please try again.";
        
        if (throwable instanceof java.net.UnknownHostException) {
            errorMessage = "No internet connection. Please check your network.";
        } else if (throwable instanceof java.net.SocketTimeoutException) {
            errorMessage = "Request timed out. Please try again.";
        } else if (throwable instanceof retrofit2.HttpException) {
            retrofit2.HttpException httpException = (retrofit2.HttpException) throwable;
            int code = httpException.code();
            
            switch (code) {
                case 401:
                    errorMessage = "Authentication failed. Please login again.";
                    break;
                case 403:
                    errorMessage = "Access denied. You don't have permission.";
                    break;
                case 404:
                    errorMessage = "Resource not found.";
                    break;
                case 500:
                    errorMessage = "Server error. Please try again later.";
                    break;
                default:
                    errorMessage = "Error " + code + ": " + httpException.message();
            }
        }
        
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
    }

    public static void handleApiResponse(Context context, ApiResponse response) {
        if (response != null && !response.isSuccess()) {
            String message = response.getMessage() != null ? response.getMessage() : "Operation failed";
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    public static void handleResponse(Context context, Response<?> response) {
        if (!response.isSuccessful()) {
            String errorMessage = "Error: " + response.code();
            if (response.errorBody() != null) {
                try {
                    errorMessage = response.errorBody().string();
                } catch (Exception e) {
                    // Use default message
                }
            }
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }
}
