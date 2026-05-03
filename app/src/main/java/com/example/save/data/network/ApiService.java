package com.example.save.data.network;

import com.example.save.data.models.MemberEntity;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface ApiService {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("members")
    Call<List<MemberEntity>> getMembers();

    @POST("members")
    Call<MemberRegistrationResponse> createMember(@Body MemberRegistrationRequest request);

    @POST("auth/admin/send-otp")
    Call<ApiResponse> sendAdminOtp(@Body OtpRequest request);

    @POST("auth/admin/verify-otp")
    Call<LoginResponse> verifyAdminOtp(@Body OtpVerificationRequest request);

    @PUT("config")
    Call<com.example.save.data.models.SystemConfig> updateSystemConfig(@Body com.example.save.data.models.SystemConfig config);

    @POST("auth/forgot-password")
    Call<ApiResponse> forgotPassword(@Body com.example.save.data.network.ForgotPasswordRequest request);

    @POST("auth/reset-password")
    Call<ApiResponse> resetPassword(@Body com.example.save.data.network.ResetPasswordRequest request);

    @GET("analytics/summary")
    Call<com.example.save.data.models.DashboardSummaryResponse> getDashboardSummary();

    @retrofit2.http.DELETE("groups")
    Call<ApiResponse> deleteGroup();

    @GET("transactions")
    Call<List<com.example.save.data.models.TransactionEntity>> getTransactions();
}
