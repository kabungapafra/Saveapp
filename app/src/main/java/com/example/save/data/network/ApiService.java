package com.example.save.data.network;

import com.example.save.data.local.entities.MemberEntity;
import com.example.save.data.local.entities.TransactionEntity;
import com.example.save.data.models.LoanRequest;
import com.example.save.data.models.Member;
import com.example.save.data.models.ComprehensiveReportResponse;

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
        /**
         * Authenticates a user (admin or member) and returns a login response with
         * access token.
         * 
         * @param request The login credentials (email/phone and password).
         * @return Call for LoginResponse.
         */
        @POST("auth/login")
        Call<LoginResponse> login(@Body LoginRequest request);

        /**
         * Sends an OTP for admin registration.
         * 
         * @param request Contains the admin's email and phone.
         * @return Call for ApiResponse.
         */
        @POST("auth/admin/send-otp")
        Call<ApiResponse> sendAdminOtp(@Body OtpRequest request);

        /**
         * Verifies the OTP and completes admin registration.
         * 
         * @param request Contains the OTP and required registration details (name,
         *                group name, etc.).
         * @return Call for LoginResponse.
         */
        @POST("auth/admin/verify-otp")
        Call<LoginResponse> verifyAdminOtp(@Body OtpVerificationRequest request);

        /**
         * Resends the OTP for admin registration.
         * 
         * @param request Contains the admin's email and phone.
         * @return Call for ApiResponse.
         */
        @POST("auth/admin/resend-otp")
        Call<ApiResponse> resendAdminOtp(@Body OtpRequest request);

        /**
         * Initiates the forgot password process by sending an OTP.
         * 
         * @param request Contains the user's email.
         * @return Call for ApiResponse.
         */
        @POST("auth/forgot-password")
        Call<ApiResponse> forgotPassword(@Body ForgotPasswordRequest request);

        /**
         * Verifies the OTP for password reset.
         * 
         * @param request Contains the user's email and the OTP received.
         * @return Call for ApiResponse.
         */
        @POST("auth/verify-reset-otp")
        Call<ApiResponse> verifyResetOtp(@Body OtpVerificationRequest request);

        /**
         * Resets the user's password after successful OTP verification.
         * 
         * @param request Contains the user's email and the new password.
         * @return Call for ApiResponse.
         */
        @POST("auth/reset-password")
        Call<ApiResponse> resetPassword(@Body ResetPasswordRequest request);

        /**
         * Resends the OTP for password reset.
         * 
         * @param request Contains the user's email.
         * @return Call for ApiResponse.
         */
        @POST("auth/resend-reset-otp")
        Call<ApiResponse> resendResetOtp(@Body ForgotPasswordRequest request);

        /**
         * Changes the user's password (requires current password).
         * 
         * @param request Contains the email, current password, and new password.
         * @return Call for ApiResponse.
         */
        @POST("auth/change-password")
        Call<ApiResponse> changePassword(@Body ChangePasswordRequest request);

        // Members
        /**
         * Fetches a list of all members in the current admin's group.
         * 
         * @return Call for list of MemberEntity.
         */
        @GET("members")
        Call<List<MemberEntity>> getMembers();

        /**
         * Fetches details for a specific member by their unique ID.
         * 
         * @param id The unique ID of the member.
         * @return Call for MemberEntity.
         */
        @GET("members/{id}")
        Call<MemberEntity> getMember(@Path("id") String id);

        /**
         * Registers a new member in the system.
         * 
         * @param request Contains member details and registration OTP.
         * @return Call for ApiResponse.
         */
        @POST("members")
        Call<ApiResponse> createMember(@Body MemberRegistrationRequest request);

        /**
         * Updates an existing member's profile information.
         * 
         * @param id      The unique ID of the member.
         * @param request Contains the fields to be updated.
         * @return Call for ApiResponse.
         */
        @PUT("members/{id}")
        Call<ApiResponse> updateMember(@Path("id") String id, @Body MemberUpdateRequest request);

        /**
         * Deletes a specific member from the system. Admin only.
         * 
         * @param id The unique ID of the member to delete.
         * @return Call for ApiResponse.
         */
        @retrofit2.http.DELETE("members/{id}")
        Call<ApiResponse> deleteMember(@Path("id") String id);

        // Loans
        /**
         * Fetches a list of loans, optionally filtered by status.
         * 
         * @param status The status to filter by (e.g., "pending", "approved").
         * @return Call for list of LoanResponse.
         */
        @GET("loans")
        Call<List<LoanResponse>> getLoans(@Query("status") String status);

        /**
         * Submits a new loan request for a member.
         * 
         * @param request The loan request details (amount, duration, reason, etc.).
         * @return Call for LoanResponse.
         */
        @POST("loans")
        Call<LoanResponse> submitLoanRequest(@Body LoanRequest request);

        /**
         * Approves a pending loan request. Admin only.
         * 
         * @param loanId  The ID of the loan to approve.
         * @param request Approval details.
         * @return Call for ApiResponse.
         */
        @POST("loans/{id}/approve")
        Call<ApiResponse> approveLoan(@Path("id") String loanId, @Body ApprovalRequest request);

        /**
         * Rejects a pending loan request. Admin only.
         * 
         * @param loanId  The ID of the loan to reject.
         * @param request Rejection details.
         * @return Call for ApiResponse.
         */
        @POST("loans/{id}/reject")
        Call<ApiResponse> rejectLoan(@Path("id") String loanId, @Body RejectionRequest request);

        /**
         * Records a loan repayment.
         * 
         * @param loanId  The ID of the loan being repaid.
         * @param request Repayment details (amount, payment method).
         * @return Call for ApiResponse.
         */
        @POST("loans/{id}/repay")
        Call<ApiResponse> repayLoan(@Path("id") String loanId, @Body LoanRepaymentRequest request);

        // Transactions
        /**
         * Fetches a list of transactions, optionally filtered by member ID and date
         * range.
         * 
         * @param memberId  The ID of the member to filter by.
         * @param startDate The start date for the range (milliseconds).
         * @param endDate   The end date for the range (milliseconds).
         * @return Call for list of TransactionEntity.
         */
        @GET("transactions")
        Call<List<TransactionEntity>> getTransactions(@Query("memberId") String memberId,
                        @Query("startDate") Long startDate,
                        @Query("endDate") Long endDate);

        /**
         * Creates a new financial transaction (e.g., contribution).
         * 
         * @param request Transaction details.
         * @return Call for TransactionEntity.
         */
        @POST("transactions")
        Call<TransactionEntity> createTransaction(@Body TransactionRequest request);

        /**
         * Approves a pending transaction. Admin only.
         * 
         * @param transactionId The ID of the transaction to approve.
         * @param request       Approval details.
         * @return Call for ApiResponse.
         */
        @POST("transactions/{id}/approve")
        Call<ApiResponse> approveTransaction(@Path("id") String transactionId, @Body ApprovalRequest request);

        // Payouts
        /**
         * Fetches the queue of members eligible for payout. Admin only.
         * 
         * @return Call for list of MemberEntity.
         */
        @GET("payouts/queue")
        Call<List<MemberEntity>> getPayoutQueue();

        /**
         * Initiates a payout for a member. Admin only.
         * 
         * @param request Payout details (amount, member email).
         * @return Call for ApiResponse.
         */
        @POST("payouts")
        Call<ApiResponse> executePayout(@Body PayoutRequest request);

        /**
         * Approves and finalizes a pending payout. Admin only.
         * 
         * @param payoutId The ID of the payout to approve.
         * @param request  Approval details.
         * @return Call for ApiResponse.
         */
        @POST("payouts/{id}/approve")
        Call<ApiResponse> approvePayout(@Path("id") String payoutId, @Body ApprovalRequest request);

        // Analytics
        /**
         * Fetches summary statistics for the admin dashboard.
         * 
         * @return Call for DashboardResponse.
         */
        @GET("analytics/dashboard")
        Call<DashboardResponse> getDashboardData();

        /**
         * Fetches financial reports for a specified date range. Admin only.
         * 
         * @param startDate Start date (milliseconds).
         * @param endDate   End date (milliseconds).
         * @return Call for ReportResponse.
         */
        @GET("analytics/reports")
        Call<ReportResponse> getReports(@Query("startDate") Long startDate,
                        @Query("endDate") Long endDate);

        /**
         * Fetches a comprehensive detailed report for the group. Admin only.
         * 
         * @return Call for ComprehensiveReportResponse.
         */
        @GET("analytics/report/comprehensive")
        Call<ComprehensiveReportResponse> getComprehensiveReport();
}
