package com.example.save.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.save.data.models.Member;
import com.example.save.data.models.MemberEntity;
import com.example.save.data.models.SystemConfig;
import com.example.save.data.models.ComprehensiveReportResponse;
import com.example.save.data.network.ApiService;
import com.example.save.data.network.RetrofitClient;
import com.example.save.data.network.LoginResponse;
import com.example.save.data.network.MemberRegistrationRequest;
import com.example.save.data.network.MemberRegistrationResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * MemberRepository - PostgreSQL Network Version
 * Transitioned back to a network-driven architecture.
 */
public class MemberRepository {
    private static MemberRepository instance;
    private final Executor executor;
    private final Context appContext;
    private final MutableLiveData<List<Member>> membersLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Double> groupBalance = new MutableLiveData__(0.0);

    private MemberRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.executor = Executors.newSingleThreadExecutor();
    }

    public static synchronized MemberRepository getInstance(Context context) {
        if (instance == null) instance = new MemberRepository(context.getApplicationContext());
        return instance;
    }

    public static synchronized MemberRepository getInstance() {
        if (instance == null) throw new IllegalStateException("Not initialized");
        return instance;
    }

    public void refreshMembers(MemberAddCallback callback) {
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.getMembers().enqueue(new Callback<List<MemberEntity>>() {
            @Override
            public void onResponse(Call<List<MemberEntity>> call, Response<List<MemberEntity>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Member> models = new ArrayList<>();
                    for (MemberEntity entity : response.body()) {
                        Member m = new Member(entity.getName(), entity.getRole(), true, entity.getPhone(), entity.getEmail());
                        m.setContributionPaid(entity.getContributionPaid());
                        m.setContributionTarget(entity.getContributionTarget());
                        models.add(m);
                    }
                    membersLiveData.postValue(models);
                    if (callback != null) callback.onResult(true, "Synced from PostgreSQL");
                } else if (callback != null) {
                    callback.onResult(false, "Failed to sync");
                }
            }

            @Override
            public void onFailure(Call<List<MemberEntity>> call, Throwable t) {
                if (callback != null) callback.onResult(false, "Network error: " + t.getMessage());
            }
        });
    }

    public void addMember(Member member, MemberRegistrationCallback callback) {
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        MemberRegistrationRequest request = new MemberRegistrationRequest(member.getName(), member.getEmail(), member.getPhone(), member.getRole());
        
        apiService.createMember(request).enqueue(new Callback<MemberRegistrationResponse>() {
            @Override
            public void onResponse(Call<MemberRegistrationResponse> call, Response<MemberRegistrationResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    refreshMembers(null);
                    if (callback != null) callback.onResult(true, "Member registered in PostgreSQL", response.body().getOtp());
                } else if (callback != null) {
                    callback.onResult(false, "Registration failed", null);
                }
            }

            @Override
            public void onFailure(Call<MemberRegistrationResponse> call, Throwable t) {
                if (callback != null) callback.onResult(false, "Network error", null);
            }
        });
    }

    public LiveData<List<Member>> getMembers() { return membersLiveData; }
    public List<Member> getAllMembers() { return membersLiveData.getValue(); }
    public Executor getExecutor() { return executor; }

    // Preservation of other stubs (to be implemented as backend grows)
    public interface MemberAddCallback { void onResult(boolean success, String message); }
    public interface MemberRegistrationCallback { void onResult(boolean success, String message, String otp); }
    public interface PasswordChangeCallback { void onResult(boolean success, String message); }
    public interface PayoutCallback { void onResult(boolean success, String message); }
    public interface ConfigCallback { void onResult(boolean success, SystemConfig config, String message); }
    public interface ReportCallback { void onResult(boolean success, ComprehensiveReportResponse report, String message); }
    public interface PaymentCallback { void onResult(boolean success, String message); }
    public interface ApprovalCallback { void onResult(boolean success, String message); }
    public interface LoanSubmissionCallback { void onResult(boolean success, String message); }
    public interface RejectionCallback { void onResult(boolean success, String message); }
    public interface LoanRepaymentCallback { void onResult(boolean success, String message); }
    public interface EligibilityCallback { void onResult(boolean success, String message); }
    public interface RepaymentScheduleCallback { void onResult(boolean success, String message); }
    public interface SummaryCallback { void onResult(boolean success, String message); }

    public int getActiveMemberCount() { return getAllMembers() != null ? getAllMembers().size() : 0; }
    public int getTotalMemberCount() { return getAllMembers() != null ? getAllMembers().size() : 0; }
    public void syncMembers() { refreshMembers(null); }
    public Member getMemberByEmail(String email) {
        if (getAllMembers() == null) return null;
        for (Member m : getAllMembers()) if (email.equals(m.getEmail())) return m;
        return null;
    }
    public Member getMemberByName(String name) {
        if (getAllMembers() == null) return null;
        for (Member m : getAllMembers()) if (name.equals(m.getName())) return m;
        return null;
    }
    public List<Member> getAdmins() {
        List<Member> admins = new ArrayList<>();
        if (getAllMembers() != null) {
            for (Member m : getAllMembers()) if ("ADMIN".equalsIgnoreCase(m.getRole())) admins.add(m);
        }
        return admins;
    }
    
    // Remaining stubs...
    public void deleteMember(Member m, MemberAddCallback cb) {}
    public void resetPassword(String e, String n, PasswordChangeCallback cb) {}
    public LiveData<Double> getGroupBalance() { return groupBalance; }
    public void updateSystemConfig(Object u, ConfigCallback cb) {}
    public void executePayout(Member m, double a, boolean d, PayoutCallback cb) {}
    public void makePayment(Member m, double a, String p, String pm, PaymentCallback cb) {}
    public void fetchSystemConfig(ConfigCallback cb) {}
    public void getComprehensiveReport(ReportCallback cb) {}
    public void getDashboardSummary(SummaryCallback cb) {}
}
