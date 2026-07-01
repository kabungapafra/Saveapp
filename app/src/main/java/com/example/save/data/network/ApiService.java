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

    @GET("analytics/dashboard/summary")
    Call<com.example.save.data.models.DashboardSummaryResponse> getDashboardSummary();

    @retrofit2.http.DELETE("groups")
    Call<ApiResponse> deleteGroup();

    @GET("transactions")
    Call<PaginatedResponse<com.example.save.data.models.TransactionEntity>> getTransactions(@Query("limit") int limit, @Query("offset") int offset);

    @POST("transactions/deposits")
    Call<com.example.save.data.models.Transaction> makeDeposit(@Body com.example.save.data.models.DepositRequest request);



    @POST("loans")
    Call<com.example.save.data.models.LoanEntity> submitLoan(@Body com.example.save.data.models.LoanRequest request);

    @GET("loans")
    Call<PaginatedResponse<com.example.save.data.models.LoanEntity>> getLoans(@Query("limit") int limit, @Query("offset") int offset);

    @GET("loans/summary")
    Call<com.example.save.data.models.LoanSummaryResponse> getLoanSummary();

    @POST("payouts")
    Call<ApiResponse> createPayout(@Body com.example.save.data.models.PayoutCreateRequest request);

    @POST("payouts/{id}/approve")
    Call<ApiResponse> approvePayout(@retrofit2.http.Path("id") String payoutId);

    @GET("payouts")
    Call<PaginatedResponse<com.example.save.data.models.PayoutEntity>> getPayouts(@Query("limit") int limit, @Query("offset") int offset);

    @GET("payouts/queue")
    Call<com.example.save.data.models.PayoutQueueResponse> getPayoutQueue();

    @PUT("payouts/queue/reorder")
    Call<ApiResponse> reorderPayoutQueue(@Body com.example.save.data.models.ReorderRequest request);

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

    @POST("auth/google-login")
    Call<LoginResponse> googleLogin(@Body GoogleLoginRequest request);

    @POST("auth/link-google")
    Call<ApiResponse> linkGoogleAccount(@Body GoogleLinkRequest request);

    @POST("auth/onboarding/check-phone")
    Call<ApiResponse> checkOnboardingPhone(@Body OnboardingCheckRequest request);

    @POST("auth/onboarding/set-password")
    Call<LoginResponse> setOnboardingPassword(@Body OnboardingSetPasswordRequest request);

    @GET("polls")
    Call<java.util.List<com.example.save.data.models.Poll>> getPolls();

    @POST("polls")
    Call<com.example.save.data.models.Poll> createPoll(@Body PollCreateRequest request);

    @POST("polls/{pollId}/vote")
    Call<com.example.save.data.models.Poll> castVote(
            @retrofit2.http.Path("pollId") String pollId,
            @Body CastVoteRequestBody request);

    @PUT("polls/{pollId}/close")
    Call<com.example.save.data.models.Poll> closePoll(
            @retrofit2.http.Path("pollId") String pollId);

    @POST("notifications/update-token")
    Call<ApiResponse> updateFcmToken(@Body com.example.save.services.SaveFirebaseMessagingService.FcmTokenRequest request);

    @retrofit2.http.GET("notifications")
    Call<java.util.List<com.example.save.data.models.Notification>> getNotifications();

    @POST("notifications/{id}/read")
    Call<ApiResponse> markNotificationRead(@retrofit2.http.Path("id") String id);

    @POST("notifications/read-all")
    Call<ApiResponse> markAllNotificationsRead();

    // ── Savings Pool ──────────────────────────────────────────────────────────

    @POST("savings-pool")
    Call<com.example.save.data.models.SavingsPool> createSavingsPool(
            @Body com.example.save.data.models.SavingsPoolRequest request);

    @GET("savings-pool/active")
    Call<com.example.save.data.models.SavingsPool> getActiveSavingsPool();

    @POST("savings-pool/{id}/join")
    Call<ApiResponse> joinSavingsPool(@retrofit2.http.Path("id") String poolId);

    @POST("savings-pool/{id}/trigger-payout")
    Call<ApiResponse> triggerPoolPayout(@retrofit2.http.Path("id") String poolId);

    @POST("savings-pool/{id}/cancel")
    Call<ApiResponse> cancelSavingsPool(@retrofit2.http.Path("id") String poolId);

    // ── Support: live chat & tickets ──────────────────────────────────────────
    @GET("support/chat/messages")
    Call<okhttp3.ResponseBody> getSupportMessages();

    @POST("support/chat/messages")
    Call<okhttp3.ResponseBody> sendSupportMessage(@Body java.util.Map<String, String> body);

    @POST("support/tickets")
    Call<okhttp3.ResponseBody> createSupportTicket(@Body java.util.Map<String, String> body);

    // ── Chat request / accept flow ─────────────────────────────────────────
    @POST("support/chat/request")
    Call<okhttp3.ResponseBody> requestChat();

    @GET("support/chat/status")
    Call<okhttp3.ResponseBody> getChatStatus();

    @POST("support/chat/end")
    Call<okhttp3.ResponseBody> endChatMember();

    // ── Admin-side chat ────────────────────────────────────────────────────
    @GET("support/admin/chat/requests")
    Call<okhttp3.ResponseBody> getAdminChatRequests();

    @POST("support/admin/chat/{conv_id}/accept")
    Call<okhttp3.ResponseBody> acceptChat(@retrofit2.http.Path("conv_id") String convId);

    @POST("support/admin/chat/{conv_id}/decline")
    Call<okhttp3.ResponseBody> declineChat(@retrofit2.http.Path("conv_id") String convId);

    @GET("support/admin/chat/{conv_id}/messages")
    Call<okhttp3.ResponseBody> getAdminChatMessages(@retrofit2.http.Path("conv_id") String convId);

    @POST("support/admin/chat/{conv_id}/messages")
    Call<okhttp3.ResponseBody> sendAdminMessage(
            @retrofit2.http.Path("conv_id") String convId,
            @Body java.util.Map<String, String> body);

    @POST("support/admin/chat/{conv_id}/end")
    Call<okhttp3.ResponseBody> endChatAdmin(@retrofit2.http.Path("conv_id") String convId);
}
