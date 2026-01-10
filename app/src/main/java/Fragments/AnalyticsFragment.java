package Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import Adapters.AnalyticsActivityAdapter;
import Data.ActivityModel;
import Data.LoanRepository;
import Data.MemberRepository;

public class AnalyticsFragment extends Fragment {

    private static final String ARG_IS_ADMIN = "is_admin";
    private boolean isAdmin = false;

    private TextView tvTitle;
    private TextView tvMetric1Label, tvMetric1Value;
    private TextView tvMetric2Label, tvMetric2Value;
    private TextView tvMetric3Label, tvMetric3Value;
    private TextView tvMetric4Label, tvMetric4Value;

    private ProgressBar progressActual;
    private TextView tvProgressValue, tvChartTitle;

    // New Sections
    private View cardLoanHealth;
    private TextView tvHealthyLoansCount, tvAtRiskLoansCount, tvOverdueLoansCount;
    private RecyclerView recyclerRecentActivity;

    // Repositories
    private LoanRepository loanRepository;
    private MemberRepository memberRepository;

    public static AnalyticsFragment newInstance(boolean isAdmin) {
        AnalyticsFragment fragment = new AnalyticsFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_ADMIN, isAdmin);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isAdmin = getArguments().getBoolean(ARG_IS_ADMIN);
        }
        loanRepository = LoanRepository.getInstance();
        memberRepository = MemberRepository.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);
        initViews(view);
        updateUI();
        return view;
    }

    private void initViews(View view) {
        tvTitle = view.findViewById(R.id.tvAnalyticsTitle);

        tvMetric1Label = view.findViewById(R.id.tvMetric1Label);
        tvMetric1Value = view.findViewById(R.id.tvMetric1Value);

        tvMetric2Label = view.findViewById(R.id.tvMetric2Label);
        tvMetric2Value = view.findViewById(R.id.tvMetric2Value);

        tvMetric3Label = view.findViewById(R.id.tvMetric3Label);
        tvMetric3Value = view.findViewById(R.id.tvMetric3Value);

        tvMetric4Label = view.findViewById(R.id.tvMetric4Label);
        tvMetric4Value = view.findViewById(R.id.tvMetric4Value);

        progressActual = view.findViewById(R.id.progressActual);
        tvProgressValue = view.findViewById(R.id.tvProgressValue);
        tvChartTitle = view.findViewById(R.id.tvChartTitle);

        // Loan Health
        cardLoanHealth = view.findViewById(R.id.cardLoanHealth);
        tvHealthyLoansCount = view.findViewById(R.id.tvHealthyLoansCount);
        tvAtRiskLoansCount = view.findViewById(R.id.tvAtRiskLoansCount);
        tvOverdueLoansCount = view.findViewById(R.id.tvOverdueLoansCount);

        // Recent Activity
        recyclerRecentActivity = view.findViewById(R.id.recyclerRecentActivity);
        recyclerRecentActivity.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void updateUI() {
        if (isAdmin) {
            setupAdminView();
        } else {
            setupMemberView();
        }
    }

    private void setupAdminView() {
        tvTitle.setText("Group Overview");

        // Metrics
        tvMetric1Label.setText("Total Savings");
        tvMetric1Value.setText(formatCurrency(5500000));

        tvMetric2Label.setText("Active Loans");
        tvMetric2Value.setText(formatCurrency(loanRepository.getTotalOutstanding()));

        tvMetric3Label.setText("Revenue");
        tvMetric3Value.setText(formatCurrency(loanRepository.getTotalInterestEarned()));

        tvMetric4Label.setText("Members");
        tvMetric4Value.setText(String.valueOf(memberRepository.getAllMembers().size()));

        // Chart
        tvChartTitle.setText("Revenue vs Monthly Target");
        int progress = 78;
        progressActual.setProgress(progress);
        tvProgressValue.setText(progress + "%");

        // Loan Health (Visible)
        cardLoanHealth.setVisibility(View.VISIBLE);
        // Mock data
        tvHealthyLoansCount.setText("12");
        tvAtRiskLoansCount.setText("2");
        tvOverdueLoansCount.setText("1");

        // Recent Activity (Group)
        List<ActivityModel> activities = new ArrayList<>();
        activities.add(new ActivityModel("Loan Repayment - Jane", "10 mins ago", 50000, true));
        activities.add(new ActivityModel("New Loan - John", "2 hrs ago", 200000, false));
        activities.add(new ActivityModel("Contribution - Sarah", "5 hrs ago", 20000, true));
        activities.add(new ActivityModel("Contribution - Mike", "1 day ago", 20000, true));

        recyclerRecentActivity.setAdapter(new AnalyticsActivityAdapter(activities));
    }

    private void setupMemberView() {
        tvTitle.setText("My Financials");

        // Metrics
        tvMetric1Label.setText("My Savings");
        tvMetric1Value.setText(formatCurrency(350000));

        tvMetric2Label.setText("Active Loan");
        tvMetric2Value.setText(formatCurrency(0));

        tvMetric3Label.setText("Dividends Earned");
        tvMetric3Value.setText(formatCurrency(15000));

        tvMetric4Label.setText("Next Contribution");
        tvMetric4Value.setText("Due in 5 days");

        // Chart
        tvChartTitle.setText("Personal Savings Goal");
        int progress = 45;
        progressActual.setProgress(progress);
        tvProgressValue.setText(progress + "%");

        // Loan Health (Hidden)
        cardLoanHealth.setVisibility(View.GONE);

        // Recent Activity (Personal)
        List<ActivityModel> activities = new ArrayList<>();
        activities.add(new ActivityModel("Monthly Contribution", "5 days ago", 20000, true));
        activities.add(new ActivityModel("Loan Repayment", "2 weeks ago", 50000, false));
        activities.add(new ActivityModel("Dividend Payout", "1 month ago", 15000, true));

        recyclerRecentActivity.setAdapter(new AnalyticsActivityAdapter(activities));
    }

    private String formatCurrency(double amount) {
        return String.format(Locale.getDefault(), "UGX %,.0f", amount);
    }
}
