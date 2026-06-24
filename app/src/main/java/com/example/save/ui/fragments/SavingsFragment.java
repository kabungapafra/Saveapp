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
    private static final String KEY_POOL_SAVE_DATE    = "pool_save_date";
    private static final String KEY_POOL_RECEIVE_DATE = "pool_receive_date";

    private FragmentSavingsBinding binding;
    private DashboardTxAdapter contribAdapter;
    private List<MemberEntity> allMembers = new ArrayList<>();
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

        setupContribList();
        restorePoolState();
        loadData();

        binding.btnActivateGroup.setOnClickListener(v -> showActivateSheet());
        binding.cardGroupInfo.setOnClickListener(v -> {
            SharedPreferences p = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            if (p.getBoolean(KEY_POOL_ACTIVE, false)) showDetailSheet();
        });
    }

    // ── restore from prefs ─────────────────────────────────────────────────────

    private void restorePoolState() {
        SharedPreferences p = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String saveDt = p.getString(KEY_POOL_SAVE_DATE, "--");
        String recvDt = p.getString(KEY_POOL_RECEIVE_DATE, "--");
        buildSavingsCalendar(saveDt, recvDt);
        if (p.getBoolean(KEY_POOL_ACTIVE, false)) {
            double amount = p.getFloat(KEY_POOL_AMOUNT, 0f);
            renderActiveCard(amount, saveDt, recvDt);
        }
    }

    // ── savings schedule calendar ──────────────────────────────────────────────

    private void buildSavingsCalendar(String saveDateStr, String receiveDateStr) {
        if (binding == null || !isAdded()) return;

        // Pick the month to display: prefer the save date month, fall back to now
        Calendar displayCal = Calendar.getInstance();
        int saveDayHighlight   = -1;
        int receiveDayHighlight = -1;

        try {
            if (saveDateStr != null && !saveDateStr.equals("--") && !saveDateStr.isEmpty()) {
                Date sd = dateFmt.parse(saveDateStr);
                Calendar sc = Calendar.getInstance();
                sc.setTime(sd);
                displayCal.set(sc.get(Calendar.YEAR), sc.get(Calendar.MONTH), 1);
                saveDayHighlight = sc.get(Calendar.DAY_OF_MONTH);

                // Legend labels
                SimpleDateFormat shortFmt = new SimpleDateFormat("d MMM", Locale.getDefault());
                binding.tvLegendSaveDate.setText(shortFmt.format(sd));
            }
        } catch (ParseException ignored) {}

        try {
            if (receiveDateStr != null && !receiveDateStr.equals("--") && !receiveDateStr.isEmpty()) {
                Date rd = dateFmt.parse(receiveDateStr);
                Calendar rc = Calendar.getInstance();
                rc.setTime(rd);
                // If receive is in a different month, keep displaying save month but mark if in same month
                if (rc.get(Calendar.MONTH) == displayCal.get(Calendar.MONTH)
                        && rc.get(Calendar.YEAR) == displayCal.get(Calendar.YEAR)) {
                    receiveDayHighlight = rc.get(Calendar.DAY_OF_MONTH);
                }
                SimpleDateFormat shortFmt = new SimpleDateFormat("d MMM", Locale.getDefault());
                binding.tvLegendReceiveDate.setText(shortFmt.format(rd));
            }
        } catch (ParseException ignored) {}

        // Month/year header
        SimpleDateFormat mFmt = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        binding.tvSavingsCalMonthYear.setText(mFmt.format(displayCal.getTime()));

        int todayDay    = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        boolean isThisMonth = displayCal.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH)
                && displayCal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR);

        int startOffset = displayCal.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = displayCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        float dp = getResources().getDisplayMetrics().density;
        int cellH = (int) (38 * dp);

        LinearLayout container = binding.containerSavingsCalRows;
        container.removeAllViews();

        int dayNum = 1;
        for (int row = 0; row < 6; row++) {
            if (dayNum > daysInMonth) break;
            LinearLayout rowView = new LinearLayout(requireContext());
            rowView.setOrientation(LinearLayout.HORIZONTAL);
            rowView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            for (int col = 0; col < 7; col++) {
                int cellIndex = row * 7 + col;
                boolean active = cellIndex >= startOffset && dayNum <= daysInMonth;
                int day = active ? dayNum : 0;

                TextView cell = new TextView(requireContext());
                LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, cellH, 1f);
                cp.setMargins((int)(2*dp), (int)(2*dp), (int)(2*dp), (int)(2*dp));
                cell.setLayoutParams(cp);
                cell.setGravity(Gravity.CENTER);
                cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);

                if (active) {
                    cell.setText(String.valueOf(day));
                    if (day == saveDayHighlight && day == receiveDayHighlight) {
                        cell.setBackground(makeCalOval("#2563EB"));
                        cell.setTextColor(Color.WHITE);
                        cell.setTypeface(null, Typeface.BOLD);
                    } else if (day == saveDayHighlight) {
                        cell.setBackground(makeCalOval("#2563EB"));
                        cell.setTextColor(Color.WHITE);
                        cell.setTypeface(null, Typeface.BOLD);
                    } else if (day == receiveDayHighlight) {
                        cell.setBackground(makeCalOval("#10B981"));
                        cell.setTextColor(Color.WHITE);
                        cell.setTypeface(null, Typeface.BOLD);
                    } else if (isThisMonth && day == todayDay) {
                        cell.setBackground(makeCalOval("#E2E8F0"));
                        cell.setTextColor(Color.parseColor("#1E3A5F"));
                        cell.setTypeface(null, Typeface.BOLD);
                    } else {
                        cell.setTextColor(Color.parseColor("#374151"));
                    }
                    dayNum++;
                } else {
                    cell.setText("");
                }
                rowView.addView(cell);
            }
            container.addView(rowView);
        }
    }

    private GradientDrawable makeCalOval(String hex) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(Color.parseColor(hex));
        return d;
    }

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
                        double amt    = p.getFloat(KEY_POOL_AMOUNT, 0f);
                        String saveDt = p.getString(KEY_POOL_SAVE_DATE, "--");
                        String recvDt = p.getString(KEY_POOL_RECEIVE_DATE, "--");
                        renderActiveCard(amt, saveDt, recvDt);
                    }
                });
            }
            @Override public void onFailure(Call<DashboardSummaryResponse> call, Throwable t) {}
        });

        api.getSystemConfig().enqueue(new Callback<SystemConfig>() {
            @Override
            public void onResponse(Call<SystemConfig> call, Response<SystemConfig> response) {
                if (!isAdded() || !response.isSuccessful() || response.body() == null) return;
                monthlyContribPerMember = response.body().getContributionAmount();
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

                // Convert server ISO dates back to "dd MMM yyyy" for display
                String sd = isoToDisplay(pool.getSaveDate());
                String rd = isoToDisplay(pool.getReceiveDate());
                requireActivity().runOnUiThread(() -> {
                    requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                            .putBoolean(KEY_POOL_ACTIVE, true)
                            .putFloat(KEY_POOL_AMOUNT, (float) pool.getTotalAmount())
                            .putString(KEY_POOL_SAVE_DATE, sd)
                            .putString(KEY_POOL_RECEIVE_DATE, rd)
                            .putString("pool_id", pool.getId())
                            .apply();
                    renderActiveCard(pool.getTotalAmount(), sd, rd);
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

    private void renderActiveCard(double totalAmount, String saveDate, String receiveDate) {
        if (binding == null) return;
        int members = totalMembers > 0 ? totalMembers : 1;
        double perMember = totalAmount / members;

        binding.tvPoolStatus.setText("Active");
        binding.tvPoolStatus.setTextColor(Color.parseColor("#065F46"));
        binding.tvPoolStatus.setBackgroundResource(R.drawable.bg_badge_success);

        binding.layoutPoolInactive.setVisibility(View.GONE);
        binding.layoutPoolActive.setVisibility(View.VISIBLE);

        binding.tvPoolSavingsAvailable.setText("UGX " + ugFmt.format(availableSavings));
        binding.tvPoolGiveOutAmount.setText("UGX " + ugFmt.format(perMember));
        binding.tvPoolMemberCount.setText(String.valueOf(members));
        binding.tvPoolSaveDate.setText(saveDate);
        binding.tvPoolReceiveDate.setText(receiveDate);

        binding.btnActivateGroup.setText("Edit Pool");
        binding.btnActivateGroup.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(Color.parseColor("#10B981")));

        buildSavingsCalendar(saveDate, receiveDate);
    }

    // ── activate bottom sheet ──────────────────────────────────────────────────

    private void showActivateSheet() {
        if (getContext() == null) return;
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        View sv = LayoutInflater.from(requireContext()).inflate(R.layout.sheet_activate_pool, null);
        sheet.setContentView(sv);

        TextInputEditText etAmount = sv.findViewById(R.id.etPoolAmount);
        TextView tvAmountPreview   = sv.findViewById(R.id.tvAmountPreview);
        TextView tvSaveDateDisplay = sv.findViewById(R.id.tvSaveDateDisplay);
        TextView tvRecvDateDisplay = sv.findViewById(R.id.tvReceiveDateDisplay);
        View rowPreview            = sv.findViewById(R.id.rowSplitPreview);
        TextView tvSplitPreview    = sv.findViewById(R.id.tvSplitPreview);
        View rowSaveDate           = sv.findViewById(R.id.rowSaveDate);
        View rowReceiveDate        = sv.findViewById(R.id.rowReceiveDate);

        final Calendar saveCal = Calendar.getInstance();
        final Calendar recvCal = Calendar.getInstance();
        recvCal.add(Calendar.MONTH, 1);

        // Pre-fill existing pool values if editing; also sync the Calendar objects
        // so the DatePicker opens on the previously chosen dates
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_POOL_ACTIVE, false)) {
            float existing = prefs.getFloat(KEY_POOL_AMOUNT, 0f);
            if (existing > 0) etAmount.setText(String.valueOf((long) existing));
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
        }

        // Live split preview as admin types the amount
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                try {
                    double total = Double.parseDouble(s.toString().trim());
                    int m = totalMembers > 0 ? totalMembers : 1;
                    tvAmountPreview.setText("UGX " + ugFmt.format(total));
                    tvSplitPreview.setText("Each of " + m + " members contributes UGX "
                            + ugFmt.format(total / m));
                    rowPreview.setVisibility(View.VISIBLE);
                } catch (NumberFormatException e) {
                    tvAmountPreview.setText("UGX 0");
                    rowPreview.setVisibility(View.GONE);
                }
            }
        });

        // Date pickers on the entire row (not just the TextView)
        rowSaveDate.setOnClickListener(dt ->
                new DatePickerDialog(requireContext(), (dp, y, m, d) -> {
                    saveCal.set(y, m, d);
                    tvSaveDateDisplay.setText(dateFmt.format(saveCal.getTime()));
                }, saveCal.get(Calendar.YEAR), saveCal.get(Calendar.MONTH),
                        saveCal.get(Calendar.DAY_OF_MONTH)).show());

        rowReceiveDate.setOnClickListener(dt ->
                new DatePickerDialog(requireContext(), (dp, y, m, d) -> {
                    recvCal.set(y, m, d);
                    tvRecvDateDisplay.setText(dateFmt.format(recvCal.getTime()));
                }, recvCal.get(Calendar.YEAR), recvCal.get(Calendar.MONTH),
                        recvCal.get(Calendar.DAY_OF_MONTH)).show());

        sv.findViewById(R.id.btnConfirmActivate).setOnClickListener(btn -> {
            String amtStr  = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
            String saveStr = tvSaveDateDisplay.getText().toString().trim();
            String recvStr = tvRecvDateDisplay.getText().toString().trim();

            if (amtStr.isEmpty()) {
                Toast.makeText(requireContext(), "Enter the total savings amount", Toast.LENGTH_SHORT).show();
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

            double total = Double.parseDouble(amtStr);
            persistAndActivate(total, saveStr, recvStr);
            sheet.dismiss();
        });

        sheet.show();
    }

    private void persistAndActivate(double totalAmount, String saveDate, String receiveDate) {
        // Save locally so the card is immediately visible even if network is slow
        requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putBoolean(KEY_POOL_ACTIVE, true)
                .putFloat(KEY_POOL_AMOUNT, (float) totalAmount)
                .putString(KEY_POOL_SAVE_DATE, saveDate)
                .putString(KEY_POOL_RECEIVE_DATE, receiveDate)
                .apply();

        // Schedule local 24h pre-approval alarm for admin
        int m = totalMembers > 0 ? totalMembers : 1;
        String amtFmt = "UGX " + ugFmt.format(totalAmount / m) + " × " + m + " members";
        PaymentScheduler.schedulePoolPayout(requireContext(), receiveDate, amtFmt);

        renderActiveCard(totalAmount, saveDate, receiveDate);
        firePoolNotifications(totalAmount, saveDate, receiveDate);

        // Persist to backend (converts "dd MMM yyyy" → ISO datetime)
        String saveDateIso  = toIso(saveDate);
        String recvDateIso  = toIso(receiveDate);
        if (saveDateIso != null && recvDateIso != null) {
            ApiService api = RetrofitClient.getClient(requireContext()).create(ApiService.class);
            com.example.save.data.models.SavingsPoolRequest req =
                    new com.example.save.data.models.SavingsPoolRequest(totalAmount, saveDateIso, recvDateIso);
            api.createSavingsPool(req).enqueue(new Callback<com.example.save.data.models.SavingsPool>() {
                @Override
                public void onResponse(Call<com.example.save.data.models.SavingsPool> call,
                                       Response<com.example.save.data.models.SavingsPool> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                                .putString("pool_id", response.body().getId())
                                .apply();
                    }
                }
                @Override public void onFailure(Call<com.example.save.data.models.SavingsPool> call, Throwable t) {}
            });
        }

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
        double totalAmt  = prefs.getFloat(KEY_POOL_AMOUNT, 0f);
        String saveDate  = prefs.getString(KEY_POOL_SAVE_DATE, "--");
        String recvDate  = prefs.getString(KEY_POOL_RECEIVE_DATE, "--");
        int members      = totalMembers > 0 ? totalMembers : 1;
        double perMember = totalAmt / members;

        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        View sv = LayoutInflater.from(requireContext()).inflate(R.layout.sheet_pool_detail, null);
        sheet.setContentView(sv);

        String groupName = prefs.getString("group_name", "Group");
        ((TextView) sv.findViewById(R.id.tvDetailPoolName)).setText(groupName + " — Savings Pool");
        ((TextView) sv.findViewById(R.id.tvDetailDates)).setText(
                "Save by " + saveDate + "  ·  Receive " + recvDate);
        ((TextView) sv.findViewById(R.id.tvDetailSavingsAvailable)).setText(
                "UGX " + ugFmt.format(availableSavings));
        ((TextView) sv.findViewById(R.id.tvDetailTarget)).setText(
                "Target: UGX " + ugFmt.format(totalAmt));
        ((TextView) sv.findViewById(R.id.tvDetailMemberCount)).setText(String.valueOf(members));
        ((TextView) sv.findViewById(R.id.tvDetailPerMember)).setText(
                "UGX " + ugFmt.format(perMember));
        ((TextView) sv.findViewById(R.id.tvDetailMembersLabel)).setText(members + " total");

        int pct = totalAmt > 0 ? (int) Math.min(100, (availableSavings / totalAmt) * 100) : 0;
        com.google.android.material.progressindicator.LinearProgressIndicator pb =
                sv.findViewById(R.id.pbPoolProgress);
        pb.setProgress(pct);
        ((TextView) sv.findViewById(R.id.tvPoolProgressPct)).setText(pct + "%");

        PoolMemberAdapter memberAdapter = new PoolMemberAdapter();
        RecyclerView rv = sv.findViewById(R.id.rvPoolMembers);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(memberAdapter);
        memberAdapter.setItems(allMembers);

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
