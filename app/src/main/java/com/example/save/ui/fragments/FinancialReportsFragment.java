package com.example.save.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.save.R;
import com.example.save.databinding.FragmentFinancialReportsBinding;
import com.example.save.ui.viewmodels.LoansViewModel;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FinancialReportsFragment extends Fragment {

    private FragmentFinancialReportsBinding binding;
    private MembersViewModel membersViewModel;
    private LoansViewModel loansViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFinancialReportsBinding.inflate(inflater, container, false);
        
        membersViewModel = new ViewModelProvider(this).get(MembersViewModel.class);
        loansViewModel = new ViewModelProvider(this).get(LoansViewModel.class);

        setupListeners();
        setupCharts();
        observeData();

        return binding.getRoot();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        binding.tabMonthly.setOnClickListener(v -> selectTab(binding.tabMonthly));
        binding.tabQuarterly.setOnClickListener(v -> selectTab(binding.tabQuarterly));
        binding.tabYearly.setOnClickListener(v -> selectTab(binding.tabYearly));

        binding.btnExportPdf.setOnClickListener(v -> Toast.makeText(getContext(), "Exporting as PDF...", Toast.LENGTH_SHORT).show());
        binding.btnExportExcel.setOnClickListener(v -> Toast.makeText(getContext(), "Exporting as Excel...", Toast.LENGTH_SHORT).show());
        binding.btnExportCsv.setOnClickListener(v -> Toast.makeText(getContext(), "Exporting as CSV...", Toast.LENGTH_SHORT).show());
        
        binding.btnViewDetails.setOnClickListener(v -> Toast.makeText(getContext(), "Viewing detailed trends...", Toast.LENGTH_SHORT).show());
    }

    private void selectTab(TextView selectedTab) {
        // Reset all tabs
        binding.tabMonthly.setBackground(null);
        binding.tabMonthly.setTextColor(Color.parseColor("#64748B"));
        binding.tabQuarterly.setBackground(null);
        binding.tabQuarterly.setTextColor(Color.parseColor("#64748B"));
        binding.tabYearly.setBackground(null);
        binding.tabYearly.setTextColor(Color.parseColor("#64748B"));

        // Set selected tab
        selectedTab.setBackgroundResource(R.drawable.bg_squircle_blue);
        selectedTab.setTextColor(Color.WHITE);

        // Update charts based on selection
        updateChartData(selectedTab.getText().toString());
    }

    private void setupCharts() {
        setupLineChart();
        setupPieChart();
    }

    private void setupLineChart() {
        LineChart chart = binding.lineChart;
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDrawGridBackground(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(Color.parseColor("#94A3B8"));
        xAxis.setTextSize(10f);

        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setGridColor(Color.parseColor("#F1F5F9"));
        chart.getAxisLeft().setDrawAxisLine(false);
        chart.getAxisLeft().setTextColor(Color.parseColor("#94A3B8"));
    }

    private void setupPieChart() {
        PieChart chart = binding.pieChart;
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);
        chart.setHoleRadius(85f);
        chart.setTransparentCircleRadius(0f);
        chart.setDrawEntryLabels(false);
        chart.setDrawCenterText(false); // We use a TextView overlay in layout
    }

    private void observeData() {
        // Using mockup values for visual fidelity as requested
        binding.tvSavingsValue.setText("$45,200.00");
        
        // Populate Interest and Loans cards with mockup data
        // These are static for this high-fidelity UI task
        binding.tvInterestValue.setText("$3,120.50");
        binding.tvLoansValue.setText("$12,800.00");

        membersViewModel.getSavingsTrend().observe(getViewLifecycleOwner(), entries -> {
            // If ViewModel has real data, we can use it, but we'll ensure the x-axis matches mockup
            setLineChartData(entries != null ? entries : getMockupLineEntries());
        });

        // Mocking Active/Inactive for now as it's a UI task
        setPieChartData(102, 22);
    }

    private List<Entry> getMockupLineEntries() {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 32000));
        entries.add(new Entry(1, 35000));
        entries.add(new Entry(2, 33000));
        entries.add(new Entry(3, 45200)); // APR High Point
        entries.add(new Entry(4, 40000));
        entries.add(new Entry(5, 42000));
        return entries;
    }

    private void setLineChartData(List<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, "Savings");
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setColor(Color.parseColor("#2563EB"));
        dataSet.setLineWidth(3f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#2563EB"));
        dataSet.setFillAlpha(10);

        LineData lineData = new LineData(dataSet);
        binding.lineChart.setData(lineData);
        
        String[] months = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN"};
        binding.lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index < 0 || index >= months.length) return "";
                return months[index];
            }
        });
        
        // Highlight logic for APR (index 3) would ideally be handled via a custom XAxis renderer 
        // but for now we ensure clean labels. 
        binding.lineChart.getXAxis().setLabelCount(6);
        
        binding.lineChart.invalidate();
        binding.lineChart.animateX(1000);
    }

    private void setPieChartData(int active, int inactive) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(active, "Active"));
        entries.add(new PieEntry(inactive, "Inactive"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{Color.parseColor("#2563EB"), Color.parseColor("#F97316")});
        dataSet.setDrawValues(false);
        dataSet.setSliceSpace(4f);

        PieData data = new PieData(dataSet);
        binding.pieChart.setData(data);
        binding.pieChart.invalidate();
        binding.pieChart.animateY(1400, Easing.EaseInOutQuad);
    }

    private void updateChartData(String filter) {
        // In a real app, this would refresh data from ViewModel based on the filter
        Toast.makeText(getContext(), "Filtering by " + filter, Toast.LENGTH_SHORT).show();
        binding.lineChart.animateX(800);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
