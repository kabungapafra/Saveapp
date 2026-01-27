package com.example.save.data.network;

import com.example.save.data.local.entities.MemberEntity;
import com.example.save.data.local.entities.TransactionEntity;
import com.example.save.data.models.LoanRequest;
import com.example.save.data.models.Member;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Authentication
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/admin/send-otp")
    Call<ApiResponse> sendAdminOtp(@Body OtpRequest request);

    @POST("auth/admin/verify-otp")
    Call<LoginResponse> verifyAdminOtp(@Body OtpVerificationRequest request);

    @POST("auth/admin/resend-otp")
    Call<ApiResponse> resendAdminOtp(@Body OtpRequest request);

    @POST("auth/forgot-password")
    Call<ApiResponse> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("auth/verify-reset-otp")
    Call<ApiResponse> verifyResetOtp(@Body OtpVerificationRequest request);

    @POST("auth/reset-password")
    Call<ApiResponse> resetPassword(@Body ResetPasswordRequest request);

    @POST("auth/resend-reset-otp")
    Call<ApiResponse> resendResetOtp(@Body ForgotPasswordRequest request);

    @POST("auth/change-password")
    Call<ApiResponse> changePassword(@Body ChangePasswordRequest request);

    // Members
    @GET("members")
    Call<List<MemberEntity>> getMembers();

    @GET("members/{id}")
    Call<MemberEntity> getMember(@Path("id") String id);

    @POST("members")
    Call<ApiResponse> createMember(@Body MemberRegistrationRequest request);

    @PUT("members/{id}")
    Call<ApiResponse> updateMember(@Path("id") String id, @Body MemberUpdateRequest request);

    // Loans
    @GET("loans")
    Call<List<LoanResponse>> getLoans(@Query("status") String status);

    @POST("loans")
    Call<LoanResponse> submitLoanRequest(@Body LoanRequest request);

    @POST("loans/{id}/approve")
    Call<ApiResponse> approveLoan(@Path("id") String loanId, @Body ApprovalRequest request);

    @POST("loans/{id}/reject")
    Call<ApiResponse> rejectLoan(@Path("id") String loanId, @Body RejectionRequest request);

    @POST("loans/{id}/repay")
    Call<ApiResponse> repayLoan(@Path("id") String loanId, @Body LoanRepaymentRequest request);

    // Transactions
    @GET("transactions")
    Call<List<TransactionEntity>> getTransactions(@Query("memberId") String memberId, 
                                                   @Query("startDate") Long startDate,
                                                   @Query("endDate") Long endDate);

    @POST("transactions")
    Call<TransactionEntity> createTransaction(@Body TransactionRequest request);

    @POST("transactions/{id}/approve")
    Call<ApiResponse> approveTransaction(@Path("id") String transactionId, @Body ApprovalRequest request);

    // Payouts
    @GET("payouts/queue")
    Call<List<MemberEntity>> getPayoutQueue();

    @POST("payouts")
    Call<ApiResponse> executePayout(@Body PayoutRequest request);

    @POST("payouts/{id}/approve")
    Call<ApiResponse> approvePayout(@Path("id") String payoutId, @Body ApprovalRequest request);

    // Analytics
    @GET("analytics/dashboard")
    Call<DashboardResponse> getDashboardData();

    @GET("analytics/reports")
    Call<ReportResponse> getReports(@Query("startDate") Long startDate, 
                                    @Query("endDate") Long endDate);
}
