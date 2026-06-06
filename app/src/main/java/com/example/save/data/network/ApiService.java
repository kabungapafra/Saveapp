package com.example.save.data.network;

import com.example.save.data.models.MemberEntity;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

import com.example.save.data.models.PaginatedResponse;
import retrofit2.http.Query;

public interface ApiService {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("members")
    Call<PaginatedResponse<MemberEntity>> getMembers(@Query("limit") int limit, @Query("offset") int offset);

    @POST("members")
    Call<MemberRegistrationResponse> createMember(@Body MemberRegistrationRequest request);

    @PUT("members/{id}")
    Call<MemberEntity> updateMember(@retrofit2.http.Path("id") String memberId, @Body MemberUpdateRequest request);

    @retrofit2.http.DELETE("members/{id}")
    Call<ApiResponse> deleteMember(@retrofit2.http.Path("id") String memberId);

    @POST("auth/admin/send-otp")
    Call<ApiResponse> sendAdminOtp(@Body OtpRequest request);

    @POST("auth/admin/verify-otp")
    Call<LoginResponse> verifyAdminOtp(@Body OtpVerificationRequest request);

    @PUT("config")
    Call<com.example.save.data.models.SystemConfig> updateSystemConfig(@Body com.example.save.data.models.SystemConfig config);

    @GET("config")
    Call<com.example.save.data.models.SystemConfig> getSystemConfig();

    @POST("auth/forgot-password")
    Call<ApiResponse> forgotPassword(@Body com.example.save.data.network.ForgotPasswordRequest request);

    @POST("auth/reset-password")
    Call<ApiResponse> resetPassword(@Body com.example.save.data.network.ResetPasswordRequest request);

    @GET("analytics/summary")
    Call<com.example.save.data.models.DashboardSummaryResponse> getDashboardSummary();

    @retrofit2.http.DELETE("groups")
    Call<ApiResponse> deleteGroup();

    @GET("transactions")
    Call<PaginatedResponse<com.example.save.data.models.TransactionEntity>> getTransactions(@Query("limit") int limit, @Query("offset") int offset);

    @POST("transactions/deposits")
    Call<com.example.save.data.models.Transaction> makeDeposit(@Body com.example.save.data.models.DepositRequest request);



    @POST("loans")
    Call<com.example.save.data.models.LoanRequest> submitLoan(@Body com.example.save.data.models.LoanRequest request);

    @GET("loans")
    Call<PaginatedResponse<com.example.save.data.models.LoanEntity>> getLoans(@Query("limit") int limit, @Query("offset") int offset);

    @GET("loans/{id}")
    Call<com.example.save.data.models.LoanEntity> getLoan(@retrofit2.http.Path("id") String loanId);

    @POST("members/{id}/invite")
    Call<ApiResponse> sendMemberInvite(@retrofit2.http.Path("id") String id);

    @POST("loans/{id}/approve")
    Call<ApiResponse> approveLoan(@retrofit2.http.Path("id") String loanId);

    @POST("loans/{id}/reject")
    Call<ApiResponse> rejectLoan(@retrofit2.http.Path("id") String loanId, @Body RejectionRequest request);

    @POST("transactions/{id}/approve")
    Call<ApiResponse> approveTransaction(@retrofit2.http.Path("id") String txId, @Body ApprovalRequestDto request);

    @POST("auth/onboarding/check-phone")
    Call<ApiResponse> checkOnboardingPhone(@Body OnboardingCheckRequest request);

    @POST("auth/onboarding/set-password")
    Call<LoginResponse> setOnboardingPassword(@Body OnboardingSetPasswordRequest request);
}
