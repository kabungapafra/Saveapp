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

    @POST("members/{id}/send-invite")
    Call<ApiResponse> sendMemberInvite(@retrofit2.http.Path("id") String memberId);

    @POST("loans")
    Call<ApiResponse> submitLoan(@Body com.example.save.data.models.LoanRequest request);

    @GET("loans")
    Call<List<com.example.save.data.models.LoanRequest>> getLoans();

    @POST("loans/{id}/approve")
    Call<ApiResponse> approveLoan(@retrofit2.http.Path("id") String loanId, @retrofit2.http.Query("admin_email") String adminEmail);

    @POST("loans/{id}/reject")
    Call<ApiResponse> rejectLoan(@retrofit2.http.Path("id") String loanId, @retrofit2.http.Query("reason") String reason);

    @POST("transactions/{id}/approve")
    Call<ApiResponse> approveTransaction(@retrofit2.http.Path("id") String txId, @retrofit2.http.Query("admin_email") String adminEmail);
}
