package com.example.save.ui.fragments;

import android.app.Application;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.data.models.DashboardSummaryResponse;
import com.example.save.data.models.MemberEntity;
import com.example.save.data.models.PaginatedResponse;
import com.example.save.data.models.SystemConfig;
import com.example.save.data.models.TransactionEntity;
import com.example.save.data.network.ApiService;
import com.example.save.data.network.RetrofitClient;
import com.example.save.data.repository.NotificationRepository;
import com.example.save.databinding.FragmentSavingsBinding;
import com.example.save.ui.adapters.DashboardTxAdapter;
import com.example.save.ui.adapters.PoolMemberAdapter;
import com.example.save.utils.NotificationHelper;
import com.example.save.utils.PaymentScheduler;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SavingsFragment extends Fragment {

    private static final String PREFS                 = "ChamaPrefs";
    private static final String KEY_POOL_ACTIVE       = "pool_active";
    private static final String KEY_POOL_AMOUNT       = "pool_total_amount";
    private static final String KEY_POOL_GIVE_OUT     = "pool_give_out";
    private static final String KEY_POOL_COLLECTED    = "pool_collected";
    private static final String KEY_POOL_IS_MEMBER    = "pool_is_member";
    private static final String KEY_POOL_SAVE_DATE    = "pool_save_date";
    private static final String KEY_POOL_RECEIVE_DATE = "pool_receive_date";
    private static final String KEY_POOL_FREQUENCY    = "pool_frequency";

    private FragmentSavingsBinding binding;
    private DashboardTxAdapter contribAdapter;
    private List<MemberEntity> allMembers = new ArrayList<>();
    // Members who explicitly joined the active pool (populated from server)
    private List<com.example.save.data.models.SavingsPool.PoolMember> poolMembers = new ArrayList<>();
    private int totalMembers = 0;
    private double availableSavings = 0;
    private double monthlyContribPerMember = 0;

    private final SimpleDateFormat dateFmt    = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private final SimpleDateFormat isoFmt     = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
    private final NumberFormat ugFmt = NumberFormat.getNumberInstance(new Locale("en", "UG"));

    public static SavingsFragment newInstance() { return new SavingsFragment(); }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSavingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ugFmt.setGroupingUsed(true);

        boolean isAdmin = "admin".equalsIgnoreCase(
                com.example.save.utils.SessionManager.getInstance(requireContext()).getUserRole());

        setupChart();
        setupContribList();
        restorePoolState();
        loadData();

        if (isAdmin) {
            binding.btnActivateGroup.setOnClickListener(v -> showActivateSheet());
        } else {
            binding.btnActivateGroup.setVisibility(View.GONE);
            // Change inactive-state description to member-friendly message
            // (the TextView is a direct child of layoutPoolInactive)
            if (binding.layoutPoolInactive.getChildCount() > 0
                    && binding.layoutPoolInactive.getChildAt(0) instanceof android.widget.TextView) {
                ((android.widget.TextView) binding.layoutPoolInactive.getChildAt(0))
                        .setText("No active savings pool yet. Your admin will activate one when it's time.");
            }
        }
        binding.btnJoinPool.setOnClickListener(v -> joinPool());
        binding.cardGroupInfo.setOnClickListener(v -> {
            SharedPreferences p = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            if (p.getBoolean(KEY_POOL_ACTIVE, false) && p.getBoolean(KEY_POOL_IS_MEMBER, false))
                showDetailSheet();
        });
    }

    // ── restore from prefs ─────────────────────────────────────────────────────

    private void restorePoolState() {
        SharedPreferences p = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!p.getBoolean(KEY_POOL_ACTIVE, false)) return;
        double giveOut   = p.getFloat(KEY_POOL_GIVE_OUT, 0f);
        String saveDt    = p.getString(KEY_POOL_SAVE_DATE, "--");
        String recvDt    = p.getString(KEY_POOL_RECEIVE_DATE, "--");
        if (p.getBoolean(KEY_POOL_IS_MEMBER, false)) {
            double collected = p.getFloat(KEY_POOL_COLLECTED, 0f);
            renderActiveCard(collected, giveOut, saveDt, recvDt);
        } else {
            renderJoinCard(giveOut, saveDt, recvDt);
        }
    }

    // ── chart ─────────────────────────────────────────────────────────────────

    private void setupChart() {
        BarChart chart = binding.chartSavingsGrowth;
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);
        chart.setFitBars(true);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        chart.setExtraBottomOffset(4f);
        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setDrawGridLines(false);
        x.setDrawAxisLine(false);
        x.setGranularity(1f);
        x.setTextColor(Color.parseColor("#94A3B8"));
        x.setTextSize(11f);
    }

    private void buildProjectionChart() {
        if (binding == null) return;
        double monthly = monthlyContribPerMember * Math.max(totalMembers, 1);
        double base    = availableSavings;
        Calendar now   = Calendar.getInstance();
        String[] labels = new String[6];
        float[]  values = new float[6];
        String[] names  = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        for (int i = 0; i < 6; i++) {
            Calendar c = (Calendar) now.clone();
            c.add(Calendar.MONTH, i);
            labels[i] = names[c.get(Calendar.MONTH)];
            values[i] = (float) (base + monthly * i);
        }
        if (base == 0 && monthly == 0) {
            for (int i = 0; i < 6; i++) values[i] = (float) (500000 * (i + 1));
        }
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 6; i++) entries.add(new BarEntry(i, values[i]));
        BarDataSet ds = new BarDataSet(entries, "");
        ds.setDrawValues(false);
        int[] colors = new int[6];
        colors[0] = Color.parseColor("#10B981");
        for (int i = 1; i < 6; i++) colors[i] = Color.parseColor("#93C5FD");
        ds.setColors(colors);
        BarData bd = new BarData(ds);
        bd.setBarWidth(0.55f);
        BarChart chart = binding.chartSavingsGrowth;
        chart.setData(bd);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.getXAxis().setLabelCount(6);
        chart.animateY(600);
        chart.invalidate();
    }

    // ── savings schedule calendar ──────────────────────────────────────────────

    // ── contributions list ─────────────────────────────────────────────────────

    private void setupContribList() {
        contribAdapter = new DashboardTxAdapter();
        binding.rvRecentContributions.setAdapter(contribAdapter);
    }

    private void populateRecentContributions(List<TransactionEntity> all) {
        List<TransactionEntity> contributions = new ArrayList<>();
        for (TransactionEntity tx : all) {
            if ("CONTRIBUTION".equalsIgnoreCase(tx.getType())) {
                contributions.add(tx);
                if (contributions.size() >= 10) break;
            }
        }
        contribAdapter.setItems(contributions);
        boolean empty = contributions.isEmpty();
        binding.rvRecentContributions.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.tvNoContributions.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    // ── data loading ───────────────────────────────────────────────────────────

    private void loadData() {
        if (getContext() == null) return;
        ApiService api = RetrofitClient.getClient(requireContext()).create(ApiService.class);

        api.getDashboardSummary().enqueue(new Callback<DashboardSummaryResponse>() {
            @Override
            public void onResponse(Call<DashboardSummaryResponse> call,
                                   Response<DashboardSummaryResponse> response) {
                if (!isAdded() || binding == null || !response.isSuccessful() || response.body() == null) return;
                DashboardSummaryResponse s = response.body();
                totalMembers     = s.getTotalMembers();
                availableSavings = s.getAvailableSavings();
                String gn = s.getGroupName();
                if (gn == null || gn.isEmpty())
                    gn = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                            .getString("group_name", "Group");
                final String name = gn;
                requireActivity().runOnUiThread(() -> {
                    binding.tvGroupName.setText(name + " — Savings Pool");
                    binding.tvGroupMembers.setText(totalMembers + " members");
                    SharedPreferences p = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
                    if (p.getBoolean(KEY_POOL_ACTIVE, false)) {
                        double giveOut = p.getFloat(KEY_POOL_GIVE_OUT, 0f);
                        String saveDt  = p.getString(KEY_POOL_SAVE_DATE, "--");
                        String recvDt  = p.getString(KEY_POOL_RECEIVE_DATE, "--");
                        if (p.getBoolean(KEY_POOL_IS_MEMBER, false)) {
                            renderActiveCard(p.getFloat(KEY_POOL_COLLECTED, 0f), giveOut, saveDt, recvDt);
                        } else {
                            renderJoinCard(giveOut, saveDt, recvDt);
                        }
                    }
                    buildProjectionChart();
                });
            }
            @Override public void onFailure(Call<DashboardSummaryResponse> call, Throwable t) {}
        });

        api.getSystemConfig().enqueue(new Callback<SystemConfig>() {
            @Override
            public void onResponse(Call<SystemConfig> call, Response<SystemConfig> response) {
                if (!isAdded() || !response.isSuccessful() || response.body() == null) return;
                monthlyContribPerMember = response.body().getContributionAmount();
                if (isAdded()) requireActivity().runOnUiThread(() -> buildProjectionChart());
            }
            @Override public void onFailure(Call<SystemConfig> call, Throwable t) {}
        });

        api.getMembers(200, 0).enqueue(new Callback<PaginatedResponse<MemberEntity>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<MemberEntity>> call,
                                   Response<PaginatedResponse<MemberEntity>> response) {
                if (!isAdded() || !response.isSuccessful() || response.body() == null) return;
                allMembers = response.body().getData() != null
                        ? response.body().getData() : new ArrayList<>();
            }
            @Override public void onFailure(Call<PaginatedResponse<MemberEntity>> call, Throwable t) {}
        });

        api.getTransactions(200, 0).enqueue(new Callback<PaginatedResponse<TransactionEntity>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<TransactionEntity>> call,
                                   Response<PaginatedResponse<TransactionEntity>> response) {
                if (!isAdded() || binding == null || !response.isSuccessful() || response.body() == null) return;
                List<TransactionEntity> all = response.body().getData();
                requireActivity().runOnUiThread(() -> populateRecentContributions(all));
            }
            @Override public void onFailure(Call<PaginatedResponse<TransactionEntity>> call, Throwable t) {}
        });

        // Sync active pool from server so state is consistent across devices
        api.getActiveSavingsPool().enqueue(new Callback<com.example.save.data.models.SavingsPool>() {
            @Override
            public void onResponse(Call<com.example.save.data.models.SavingsPool> call,
                                   Response<com.example.save.data.models.SavingsPool> response) {
                if (!isAdded() || binding == null) return;
                com.example.save.data.models.SavingsPool pool =
                        (response.isSuccessful()) ? response.body() : null;
                if (pool == null) return;

                String sd = isoToDisplay(pool.getSaveDate());
                String rd = isoToDisplay(pool.getReceiveDate());

                // Recompute correct values from contribPerPeriod + dates + frequency.
                // DB values (total_amount, per_member_amount) may be stale if the pool
                // was created during a race condition where member count was wrong.
                double contrib   = pool.getContribPerPeriod();
                String freqStr   = pool.getFrequency() != null ? pool.getFrequency() : "monthly";
                long daysBetween = 30;
                try {
                    Date ds = isoFmt.parse(pool.getSaveDate().length() > 19
                            ? pool.getSaveDate().substring(0, 19) : pool.getSaveDate());
                    Date dr = isoFmt.parse(pool.getReceiveDate().length() > 19
                            ? pool.getReceiveDate().substring(0, 19) : pool.getReceiveDate());
                    if (ds != null && dr != null)
                        daysBetween = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(dr.getTime() - ds.getTime());
                } catch (ParseException ignored) {}

                int periods;
                switch (freqStr) {
                    case "daily":  periods = (int) Math.max(daysBetween, 1); break;
                    case "weekly": periods = (int) Math.max(daysBetween / 7, 1); break;
                    default:       periods = (int) Math.max(daysBetween / 30, 1); break;
                }

                // Use the best available member count
                int members = allMembers.size() > 0 ? allMembers.size()
                            : totalMembers > 0 ? totalMembers
                            : Math.max(pool.getMemberCount(), 1);

                double giveOut;
                double poolTotal;
                if (contrib > 0) {
                    // New pools: recompute from contrib_per_period
                    giveOut   = contrib * periods;
                    poolTotal = giveOut * members;
                } else {
                    // Old pools without contrib_per_period: use stored values
                    giveOut   = pool.getPerMemberAmount();
                    poolTotal = giveOut * members;
                }

                final double finalGiveOut   = giveOut;
                final double finalCollected = pool.getAmountCollected();
                final boolean isMember      = pool.isMember();
                // Store joined pool members for detail sheet
                poolMembers = pool.getJoinedMembers();
                final int joinedCount = poolMembers.size() > 0
                        ? poolMembers.size()
                        : Math.max(pool.getMemberCount(), 1);

                requireActivity().runOnUiThread(() -> {
                    requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                            .putBoolean(KEY_POOL_ACTIVE, true)
                            .putBoolean(KEY_POOL_IS_MEMBER, isMember)
                            .putFloat(KEY_POOL_AMOUNT, (float) poolTotal)
                            .putFloat(KEY_POOL_GIVE_OUT, (float) finalGiveOut)
                            .putFloat(KEY_POOL_COLLECTED, (float) finalCollected)
                            .putString(KEY_POOL_SAVE_DATE, sd)
                            .putString(KEY_POOL_RECEIVE_DATE, rd)
                            .putString("pool_id", pool.getId())
                            .putInt("pool_joined_count", joinedCount)
                            .apply();
                    if (isMember) {
                        renderActiveCard(finalCollected, finalGiveOut, sd, rd);
                    } else {
                        renderJoinCard(finalGiveOut, sd, rd);
                    }
                });
            }
            @Override public void onFailure(Call<com.example.save.data.models.SavingsPool> call, Throwable t) {}
        });
    }

    private String isoToDisplay(String iso) {
        if (iso == null) return "--";
        try {
            Date d = isoFmt.parse(iso.length() > 19 ? iso.substring(0, 19) : iso);
            return dateFmt.format(d);
        } catch (ParseException e) {
            return iso;
        }
    }

    // ── card rendering ─────────────────────────────────────────────────────────

    private void renderJoinCard(double giveOut, String saveDate, String receiveDate) {
        if (binding == null) return;
        binding.tvPoolStatus.setText("Active");
        binding.tvPoolStatus.setTextColor(Color.parseColor("#065F46"));
        binding.tvPoolStatus.setBackgroundResource(R.drawable.bg_badge_success);

        binding.layoutPoolInactive.setVisibility(View.GONE);
        binding.layoutPoolActive.setVisibility(View.GONE);
        binding.layoutPoolJoin.setVisibility(View.VISIBLE);

        binding.tvJoinGiveOut.setText("UGX " + ugFmt.format(giveOut));
        int joined = poolMembers.size() > 0 ? poolMembers.size()
                   : requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                       .getInt("pool_joined_count", 1);
        binding.tvJoinMemberCount.setText(String.valueOf(joined));
        binding.tvJoinSaveDate.setText(saveDate);
        binding.tvJoinReceiveDate.setText(receiveDate);

        binding.btnActivateGroup.setVisibility(View.GONE);
    }

    private void joinPool() {
        SharedPreferences p = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String poolId = p.getString("pool_id", null);
        if (poolId == null) { Toast.makeText(getContext(), "Pool not found", Toast.LENGTH_SHORT).show(); return; }

        binding.btnJoinPool.setEnabled(false);
        binding.btnJoinPool.setText("Joining…");

        ApiService api = RetrofitClient.getClient(requireContext()).create(ApiService.class);
        api.joinSavingsPool(poolId).enqueue(new Callback<com.example.save.data.network.ApiResponse>() {
            @Override
            public void onResponse(Call<com.example.save.data.network.ApiResponse> call,
                                   Response<com.example.save.data.network.ApiResponse> response) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        p.edit().putBoolean(KEY_POOL_IS_MEMBER, true).apply();
                        Toast.makeText(getContext(), "You joined the pool!", Toast.LENGTH_SHORT).show();
                        // Reload from server to get fresh state
                        loadData();
                    } else {
                        binding.btnJoinPool.setEnabled(true);
                        binding.btnJoinPool.setText("Join this Pool");
                        String err = response.code() == 400 ? "Already joined" : "Failed to join";
                        Toast.makeText(getContext(), err, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override public void onFailure(Call<com.example.save.data.network.ApiResponse> call, Throwable t) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    binding.btnJoinPool.setEnabled(true);
                    binding.btnJoinPool.setText("Join this Pool");
                    Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void renderActiveCard(double collected, double giveOutPerMember, String saveDate, String receiveDate) {
        if (binding == null) return;

        binding.tvPoolStatus.setText("Active");
        binding.tvPoolStatus.setTextColor(Color.parseColor("#065F46"));
        binding.tvPoolStatus.setBackgroundResource(R.drawable.bg_badge_success);

        binding.layoutPoolInactive.setVisibility(View.GONE);
        binding.layoutPoolJoin.setVisibility(View.GONE);
        binding.layoutPoolActive.setVisibility(View.VISIBLE);

        // COLLECTED = actual money paid in so far (0 until members start contributing)
        binding.tvPoolSavingsAvailable.setText("UGX " + ugFmt.format(collected));
        binding.tvPoolGiveOutAmount.setText("UGX " + ugFmt.format(giveOutPerMember));
        // Use joined pool members (from pool API) — not total group members.
        int displayCount = poolMembers != null && !poolMembers.isEmpty()
                ? poolMembers.size()
                : requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                        .getInt("pool_joined_count", totalMembers > 0 ? totalMembers : 1);
        binding.tvPoolMemberCount.setText(String.valueOf(displayCount));
        binding.tvPoolSaveDate.setText(saveDate);
        binding.tvPoolReceiveDate.setText(receiveDate);

        boolean adminRole = "admin".equalsIgnoreCase(
                com.example.save.utils.SessionManager.getInstance(requireContext()).getUserRole());
        if (adminRole) {
            binding.btnActivateGroup.setVisibility(View.VISIBLE);
            binding.btnActivateGroup.setText("Edit Pool");
            binding.btnActivateGroup.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#10B981")));
        } else {
            binding.btnActivateGroup.setVisibility(View.GONE);
        }
    }

    // ── activate bottom sheet ──────────────────────────────────────────────────

    private void showActivateSheet() {
        if (getContext() == null) return;
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        View sv = LayoutInflater.from(requireContext()).inflate(R.layout.sheet_activate_pool, null);
        sheet.setContentView(sv);

        TextInputEditText etAmount     = sv.findViewById(R.id.etPoolAmount);
        TextView tvAmountPreview       = sv.findViewById(R.id.tvAmountPreview);
        TextView tvSaveDateDisplay     = sv.findViewById(R.id.tvSaveDateDisplay);
        TextView tvRecvDateDisplay     = sv.findViewById(R.id.tvReceiveDateDisplay);
        View rowPreview                = sv.findViewById(R.id.rowSplitPreview);
        TextView tvSplitPreview        = sv.findViewById(R.id.tvSplitPreview);
        View rowSaveDate               = sv.findViewById(R.id.rowSaveDate);
        View rowReceiveDate            = sv.findViewById(R.id.rowReceiveDate);
        com.google.android.material.chip.ChipGroup chipGroupFreq = sv.findViewById(R.id.chipGroupFrequency);

        final Calendar saveCal  = Calendar.getInstance();
        final Calendar recvCal  = Calendar.getInstance();
        recvCal.add(Calendar.MONTH, 1);
        final String[] freq = {"monthly"};

        // Recalculate give-out preview whenever amount, frequency, or dates change
        Runnable updatePreview = () -> {
            String raw = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
            try {
                double contrib = Double.parseDouble(raw);
                int m = Math.max(totalMembers, 1);
                long daysBetween = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(
                        recvCal.getTimeInMillis() - saveCal.getTimeInMillis());
                int periods;
                String periodLabel;
                switch (freq[0]) {
                    case "daily":
                        periods = (int) Math.max(daysBetween, 1);
                        periodLabel = "day";
                        break;
                    case "weekly":
                        periods = (int) Math.max(daysBetween / 7, 1);
                        periodLabel = "week";
                        break;
                    default:
                        periods = (int) Math.max(daysBetween / 30, 1);
                        periodLabel = "month";
                        break;
                }
                double giveOut = contrib * periods;
                tvAmountPreview.setText("UGX " + ugFmt.format(contrib) + "/" + periodLabel);
                tvSplitPreview.setText(
                        "Give-out per member: UGX " + ugFmt.format(giveOut)
                        + "\n" + periods + " " + periodLabel + (periods > 1 ? "s" : "")
                        + " × UGX " + ugFmt.format(contrib));
                rowPreview.setVisibility(View.VISIBLE);
            } catch (NumberFormatException e) {
                tvAmountPreview.setText("UGX 0");
                rowPreview.setVisibility(View.GONE);
            }
        };

        // Pre-fill existing pool values if editing
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_POOL_ACTIVE, false)) {
            // Back-calculate contribution per period from stored total
            String storedFreq = prefs.getString(KEY_POOL_FREQUENCY, "monthly");
            freq[0] = storedFreq;
            if (storedFreq.equals("daily"))        sv.findViewById(R.id.chipDaily).performClick();
            else if (storedFreq.equals("weekly"))  sv.findViewById(R.id.chipWeekly).performClick();
            else                                   ((com.google.android.material.chip.Chip) sv.findViewById(R.id.chipMonthly)).setChecked(true);

            String sd = prefs.getString(KEY_POOL_SAVE_DATE, null);
            String rd = prefs.getString(KEY_POOL_RECEIVE_DATE, null);
            if (sd != null && !sd.equals("--")) {
                tvSaveDateDisplay.setText(sd);
                try { saveCal.setTime(dateFmt.parse(sd)); } catch (ParseException ignored) {}
            }
            if (rd != null && !rd.equals("--")) {
                tvRecvDateDisplay.setText(rd);
                try { recvCal.setTime(dateFmt.parse(rd)); } catch (ParseException ignored) {}
            }
            // Back-calculate contrib per period from stored give-out and periods
            float storedGiveOut = prefs.getFloat(KEY_POOL_GIVE_OUT, 0f);
            if (storedGiveOut > 0) {
                long days = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(
                        recvCal.getTimeInMillis() - saveCal.getTimeInMillis());
                int p = storedFreq.equals("daily") ? (int)Math.max(days,1)
                      : storedFreq.equals("weekly") ? (int)Math.max(days/7,1)
                      : (int)Math.max(days/30,1);
                long contrib = Math.round(storedGiveOut / p);
                etAmount.setText(String.valueOf(contrib));
            }
        }

        // Chip listener
        chipGroupFreq.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipDaily))        freq[0] = "daily";
            else if (checkedIds.contains(R.id.chipWeekly)) freq[0] = "weekly";
            else                                            freq[0] = "monthly";
            updatePreview.run();
        });

        // Amount watcher
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { updatePreview.run(); }
        });

        // Date pickers — re-run preview after a date is chosen
        rowSaveDate.setOnClickListener(dt ->
                new DatePickerDialog(requireContext(), (dp, y, m, d) -> {
                    saveCal.set(y, m, d);
                    tvSaveDateDisplay.setText(dateFmt.format(saveCal.getTime()));
                    updatePreview.run();
                }, saveCal.get(Calendar.YEAR), saveCal.get(Calendar.MONTH),
                        saveCal.get(Calendar.DAY_OF_MONTH)).show());

        rowReceiveDate.setOnClickListener(dt ->
                new DatePickerDialog(requireContext(), (dp, y, m, d) -> {
                    recvCal.set(y, m, d);
                    tvRecvDateDisplay.setText(dateFmt.format(recvCal.getTime()));
                    updatePreview.run();
                }, recvCal.get(Calendar.YEAR), recvCal.get(Calendar.MONTH),
                        recvCal.get(Calendar.DAY_OF_MONTH)).show());

        sv.findViewById(R.id.btnConfirmActivate).setOnClickListener(btn -> {
            String amtStr  = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
            String saveStr = tvSaveDateDisplay.getText().toString().trim();
            String recvStr = tvRecvDateDisplay.getText().toString().trim();

            if (amtStr.isEmpty()) {
                Toast.makeText(requireContext(), "Enter contribution per " + freq[0], Toast.LENGTH_SHORT).show();
                return;
            }
            if (saveStr.equals("Tap to pick")) {
                Toast.makeText(requireContext(), "Pick a saving date", Toast.LENGTH_SHORT).show();
                return;
            }
            if (recvStr.equals("Tap to pick")) {
                Toast.makeText(requireContext(), "Pick a receiving date", Toast.LENGTH_SHORT).show();
                return;
            }

            double contrib = Double.parseDouble(amtStr);
            persistAndActivate(contrib, freq[0], saveStr, recvStr);
            sheet.dismiss();
        });

        sheet.show();
    }

    private void persistAndActivate(double contribPerPeriod, String frequency, String saveDate, String receiveDate) {
        String saveDateIso = toIso(saveDate);
        String recvDateIso = toIso(receiveDate);
        if (saveDateIso == null || recvDateIso == null) {
            Toast.makeText(requireContext(), "Invalid dates", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show optimistic local estimate while waiting for server (use loaded member count or 1)
        int m = Math.max(totalMembers, 1);
        long daysBetween;
        try {
            Date sd = dateFmt.parse(saveDate);
            Date rd = dateFmt.parse(receiveDate);
            daysBetween = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(rd.getTime() - sd.getTime());
        } catch (ParseException e) { daysBetween = 30; }
        int periods;
        switch (frequency) {
            case "daily":  periods = (int) Math.max(daysBetween, 1); break;
            case "weekly": periods = (int) Math.max(daysBetween / 7, 1); break;
            default:       periods = (int) Math.max(daysBetween / 30, 1); break;
        }
        double giveOutEstimate   = contribPerPeriod * periods;
        double totalEstimate     = giveOutEstimate * m;

        // Admin who activates becomes first pool member automatically
        requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putBoolean(KEY_POOL_ACTIVE, true)
                .putBoolean(KEY_POOL_IS_MEMBER, true)
                .putFloat(KEY_POOL_AMOUNT, (float) totalEstimate)
                .putFloat(KEY_POOL_GIVE_OUT, (float) giveOutEstimate)
                .putFloat(KEY_POOL_COLLECTED, 0f)
                .putString(KEY_POOL_SAVE_DATE, saveDate)
                .putString(KEY_POOL_RECEIVE_DATE, receiveDate)
                .putString(KEY_POOL_FREQUENCY, frequency)
                .apply();

        renderActiveCard(0, giveOutEstimate, saveDate, receiveDate);
        firePoolNotifications(totalEstimate, saveDate, receiveDate);
        PaymentScheduler.schedulePoolPayout(requireContext(), receiveDate,
                "UGX " + ugFmt.format(giveOutEstimate) + " per member");

        // Send to backend — server computes exact totals from DB member count
        ApiService api = RetrofitClient.getClient(requireContext()).create(ApiService.class);
        com.example.save.data.models.SavingsPoolRequest req =
                new com.example.save.data.models.SavingsPoolRequest(contribPerPeriod, frequency, saveDateIso, recvDateIso);
        api.createSavingsPool(req).enqueue(new Callback<com.example.save.data.models.SavingsPool>() {
            @Override
            public void onResponse(Call<com.example.save.data.models.SavingsPool> call,
                                   Response<com.example.save.data.models.SavingsPool> response) {
                if (!isAdded() || binding == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    com.example.save.data.models.SavingsPool pool = response.body();
                    String sd2 = isoToDisplay(pool.getSaveDate());
                    String rd2 = isoToDisplay(pool.getReceiveDate());
                    // give-out = contribPerPeriod × periods (server computed)
                    double confirmedGiveOut    = pool.getPerMemberAmount();
                    double confirmedCollected  = pool.getAmountCollected();
                    int confirmedMembers = allMembers.size() > 0 ? allMembers.size()
                                        : totalMembers > 0 ? totalMembers
                                        : Math.max(pool.getMemberCount(), 1);
                    double confirmedTotal = confirmedGiveOut * confirmedMembers;
                    requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                            .putFloat(KEY_POOL_AMOUNT, (float) confirmedTotal)
                            .putFloat(KEY_POOL_GIVE_OUT, (float) confirmedGiveOut)
                            .putFloat(KEY_POOL_COLLECTED, (float) confirmedCollected)
                            .putString("pool_id", pool.getId())
                            .apply();
                    requireActivity().runOnUiThread(() ->
                            renderActiveCard(confirmedCollected, confirmedGiveOut, sd2, rd2));
                }
            }
            @Override public void onFailure(Call<com.example.save.data.models.SavingsPool> call, Throwable t) {}
        });

        Toast.makeText(requireContext(), "Savings pool activated!", Toast.LENGTH_SHORT).show();
    }

    private String toIso(String displayDate) {
        try {
            Date d = dateFmt.parse(displayDate);
            return isoFmt.format(d);
        } catch (ParseException e) {
            return null;
        }
    }

    // ── pool detail bottom sheet ───────────────────────────────────────────────

    private void showDetailSheet() {
        if (getContext() == null) return;
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        double totalAmt   = prefs.getFloat(KEY_POOL_AMOUNT, 0f);
        double giveOut    = prefs.getFloat(KEY_POOL_GIVE_OUT, 0f);
        double collected  = prefs.getFloat(KEY_POOL_COLLECTED, 0f);
        String saveDate   = prefs.getString(KEY_POOL_SAVE_DATE, "--");
        String recvDate   = prefs.getString(KEY_POOL_RECEIVE_DATE, "--");
        int members       = poolMembers.size() > 0 ? poolMembers.size() : 1;

        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        View sv = LayoutInflater.from(requireContext()).inflate(R.layout.sheet_pool_detail, null);
        sheet.setContentView(sv);

        String groupName = prefs.getString("group_name", "Group");
        ((TextView) sv.findViewById(R.id.tvDetailPoolName)).setText(groupName + " — Savings Pool");
        ((TextView) sv.findViewById(R.id.tvDetailDates)).setText(
                "Save by " + saveDate + "  ·  Receive " + recvDate);
        // Show actual collected amount (0 until members pay)
        ((TextView) sv.findViewById(R.id.tvDetailSavingsAvailable)).setText(
                "UGX " + ugFmt.format(collected));
        ((TextView) sv.findViewById(R.id.tvDetailTarget)).setText(
                "Target: UGX " + ugFmt.format(totalAmt));
        ((TextView) sv.findViewById(R.id.tvDetailMemberCount)).setText(String.valueOf(members));
        ((TextView) sv.findViewById(R.id.tvDetailPerMember)).setText(
                "UGX " + ugFmt.format(giveOut));
        ((TextView) sv.findViewById(R.id.tvDetailMembersLabel)).setText(members + " total");

        // Progress: collected vs target
        int pct = totalAmt > 0 ? (int) Math.min(100, (collected / totalAmt) * 100) : 0;
        com.google.android.material.progressindicator.LinearProgressIndicator pb =
                sv.findViewById(R.id.pbPoolProgress);
        pb.setProgress(pct);
        ((TextView) sv.findViewById(R.id.tvPoolProgressPct)).setText(pct + "%");

        PoolMemberAdapter memberAdapter = new PoolMemberAdapter();
        RecyclerView rv = sv.findViewById(R.id.rvPoolMembers);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(memberAdapter);

        if (!poolMembers.isEmpty()) {
            memberAdapter.setItems(poolMembers);
        } else {
            // Pool members not loaded yet — re-fetch active pool to get them
            ApiService api = RetrofitClient.getClient(requireContext()).create(ApiService.class);
            api.getActiveSavingsPool().enqueue(new Callback<com.example.save.data.models.SavingsPool>() {
                @Override
                public void onResponse(Call<com.example.save.data.models.SavingsPool> call,
                                       Response<com.example.save.data.models.SavingsPool> response) {
                    if (!isAdded() || response.body() == null) return;
                    poolMembers = response.body().getJoinedMembers();
                    requireActivity().runOnUiThread(() -> memberAdapter.setItems(poolMembers));
                }
                @Override public void onFailure(Call<com.example.save.data.models.SavingsPool> call, Throwable t) {}
            });
        }

        sheet.show();
    }

    // ── notifications ──────────────────────────────────────────────────────────

    private void firePoolNotifications(double totalAmount, String saveDate, String receiveDate) {
        int m = totalMembers > 0 ? totalMembers : 1;
        double each  = totalAmount / m;
        String title = "Savings Pool Activated";
        String body  = "Contribute UGX " + ugFmt.format(each) + " by " + saveDate
                + ". Everyone receives their share on " + receiveDate + ".";

        new NotificationHelper(requireContext())
                .showNotification(title, body, NotificationHelper.CHANNEL_ID_PAYMENTS);
        NotificationRepository.getInstance(
                        (Application) requireContext().getApplicationContext())
                .addNotification(title, body, "SAVINGS");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
