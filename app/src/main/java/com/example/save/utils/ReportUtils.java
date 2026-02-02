package com.example.save.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.save.data.models.Member;
import com.example.save.data.models.ComprehensiveReportResponse;

public class ReportUtils {

    public static void generateAndShareReport(Context context, ComprehensiveReportResponse report) {
        PdfDocument document = new PdfDocument();
        // A4 standard width is 595, height 842.
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        int y = 50;
        int pageNumber = 1;

        // --- Header Section ---
        // Header Background
        Paint headerBgPaint = new Paint();
        headerBgPaint.setColor(Color.parseColor("#4A00E0")); // Professional Deep Blue/Purple
        canvas.drawRect(0, 0, 595, 120, headerBgPaint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(24);
        paint.setFakeBoldText(true);
        canvas.drawText("GROUP FINANCIAL REPORT", 40, 60, paint);

        paint.setTextSize(14);
        paint.setFakeBoldText(false);
        String groupName = report.getGroupName() != null ? report.getGroupName().toUpperCase() : "YOUR GROUP";
        canvas.drawText(groupName, 40, 85, paint);

        paint.setTextSize(10);
        paint.setAlpha(200);
        String dateStr = "Generated on: "
                + new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(new Date());
        canvas.drawText(dateStr, 40, 105, paint);
        paint.setAlpha(255);

        y = 150;

        // --- Executive Summary (Grid Layout) ---
        paint.setColor(Color.parseColor("#333333"));
        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        canvas.drawText("EXECUTIVE SUMMARY", 40, y, paint);
        y += 30;

        // Summary Boxes
        int boxWidth = 240;
        int boxHeight = 70;
        int gap = 30;

        // Row 1
        drawSummaryBox(canvas, "Total Balance", formatCurrency(report.getTotalBalance()), 40, y, boxWidth, boxHeight,
                "#E8EAF6");
        drawSummaryBox(canvas, "Total Contributions", formatCurrency(report.getTotalContributions()),
                40 + boxWidth + gap, y, boxWidth, boxHeight, "#E0F2F1");
        y += boxHeight + 20;

        // Row 2
        drawSummaryBox(canvas, "Active Loans (" + report.getActiveLoansCount() + ")",
                formatCurrency(report.getActiveLoansAmount()), 40, y, boxWidth, boxHeight, "#FFF3E0");
        drawSummaryBox(canvas, "Total Payouts", formatCurrency(report.getTotalPayoutsAmount()), 40 + boxWidth + gap, y,
                boxWidth, boxHeight, "#FCE4EC");
        y += boxHeight + 40;

        // --- Financial Activity Overview (Micro Charts/Stats) ---
        paint.setColor(Color.BLACK);
        paint.setTextSize(14);
        paint.setFakeBoldText(true);
        canvas.drawText("GROUP STATISTICS", 40, y, paint);
        y += 20;

        paint.setTextSize(12);
        paint.setFakeBoldText(false);
        canvas.drawText("• Total Members: " + report.getTotalMembers(), 50, y, paint);
        y += 20;
        canvas.drawText("• Active Loan Volume: " + formatCurrency(report.getActiveLoansAmount()), 50, y, paint);
        y += 20;
        canvas.drawText(
                "• Net Financial Position: " + formatCurrency(report.getTotalBalance() + report.getActiveLoansAmount()),
                50, y, paint);
        y += 40;

        // --- Active Loans Table ---
        if (checkPageBreak(y)) {
            document.finishPage(page);
            pageInfo = new PdfDocument.PageInfo.Builder(595, 842, ++pageNumber).create();
            page = document.startPage(pageInfo);
            canvas = page.getCanvas();
            y = 50;
        }

        drawSectionHeader(canvas, "ACTIVE LOANS", y);
        y += 30;

        // Table Header
        drawTableHeader(canvas, y, new String[] { "Member", "Amount", "Balance", "Due Date" });
        y += 25;

        if (report.getActiveLoans() != null && !report.getActiveLoans().isEmpty()) {
            for (com.example.save.data.models.Loan loan : report.getActiveLoans()) {
                if (checkPageBreak(y)) {
                    document.finishPage(page);
                    pageInfo = new PdfDocument.PageInfo.Builder(595, 842, ++pageNumber).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 50;
                    drawTableHeader(canvas, y, new String[] { "Member", "Amount", "Balance", "Due Date" });
                    y += 25;
                }

                double balance = loan.getTotalDue() - loan.getRepaidAmount();
                String dueStr = loan.getDueDate() != null
                        ? new SimpleDateFormat("dd MMM", Locale.getDefault()).format(loan.getDueDate())
                        : "-";

                drawTableRow(canvas, y, new String[] {
                        truncate(loan.getMemberName(), 18),
                        formatCurrency(loan.getAmount()),
                        formatCurrency(balance),
                        dueStr
                });
                y += 20;
            }
        } else {
            drawTableRow(canvas, y, new String[] { "No active loans", "-", "-", "-" });
            y += 20;
        }
        y += 40;

        // --- Payouts Table ---
        if (checkPageBreak(y)) {
            document.finishPage(page);
            pageInfo = new PdfDocument.PageInfo.Builder(595, 842, ++pageNumber).create();
            page = document.startPage(pageInfo);
            canvas = page.getCanvas();
            y = 50;
        }

        drawSectionHeader(canvas, "RECENT PAYOUTS", y);
        y += 30;
        drawTableHeader(canvas, y, new String[] { "Member", "Amount", "Desc", "Date" });
        y += 25;

        if (report.getPayouts() != null && !report.getPayouts().isEmpty()) {
            for (com.example.save.data.local.entities.TransactionEntity tx : report.getPayouts()) {
                if (checkPageBreak(y)) {
                    document.finishPage(page);
                    pageInfo = new PdfDocument.PageInfo.Builder(595, 842, ++pageNumber).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 50;
                    drawTableHeader(canvas, y, new String[] { "Member", "Amount", "Desc", "Date" });
                    y += 25;
                }

                String dateTx = tx.getDate() != null
                        ? new SimpleDateFormat("dd MMM", Locale.getDefault()).format(tx.getDate())
                        : "-";
                // TransactionEntity has memberName, not memberId
                String memberDisplay = tx.getMemberName() != null ? truncate(tx.getMemberName(), 15) : "Unknown";

                drawTableRow(canvas, y, new String[] {
                        memberDisplay,
                        formatCurrency(tx.getAmount()),
                        truncate(tx.getDescription(), 20),
                        dateTx
                });
                y += 20;
            }
        } else {
            drawTableRow(canvas, y, new String[] { "No recent payouts", "-", "-", "-" });
            y += 20;
        }
        y += 40;

        // --- Member List Table ---
        if (checkPageBreak(y)) {
            document.finishPage(page);
            pageInfo = new PdfDocument.PageInfo.Builder(595, 842, ++pageNumber).create();
            page = document.startPage(pageInfo);
            canvas = page.getCanvas();
            y = 50;
        }

        drawSectionHeader(canvas, "MEMBER DIRECTORY", y);
        y += 30;
        drawTableHeader(canvas, y, new String[] { "Name", "Contact", "Paid", "Status" });
        y += 25;

        if (report.getMembers() != null) {
            for (Member member : report.getMembers()) {
                if (checkPageBreak(y)) {
                    document.finishPage(page);
                    pageInfo = new PdfDocument.PageInfo.Builder(595, 842, ++pageNumber).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 50;
                    drawTableHeader(canvas, y, new String[] { "Name", "Contact", "Paid", "Status" });
                    y += 25;
                }

                String status = member.hasReceivedPayout() ? "Paid Out" : (member.isActive() ? "Active" : "Inactive");

                drawTableRow(canvas, y, new String[] {
                        truncate(member.getName(), 18),
                        member.getPhone(),
                        formatCurrency(member.getContributionPaid()),
                        status
                });
                y += 20;
            }
        }

        // --- Footer ---
        paint.setColor(Color.LTGRAY);
        paint.setTextSize(10);
        canvas.drawText("End of Report", 270, 820, paint);

        document.finishPage(page);

        // Save
        String fileName = "Group_Report_" + System.currentTimeMillis() + ".pdf";
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);

        try {
            document.writeTo(new FileOutputStream(file));
            document.close();
            shareFile(context, file);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error generating report", Toast.LENGTH_SHORT).show();
        }
    }

    // --- Helper Methods ---

    private static void drawSectionHeader(Canvas canvas, String title, int y) {
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#4A00E0"));
        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        canvas.drawText(title, 40, y, paint);
        paint.setStrokeWidth(2);
        canvas.drawLine(40, y + 5, 555, y + 5, paint);
    }

    private static void drawSummaryBox(Canvas canvas, String label, String value, int x, int y, int width, int height,
            String colorHex) {
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.parseColor(colorHex));
        bgPaint.setStyle(Paint.Style.FILL);

        // Draw rounded rect equivalent (simplified for standard PDF)
        canvas.drawRect(x, y, x + width, y + height, bgPaint);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#616161"));
        textPaint.setTextSize(12);
        canvas.drawText(label, x + 15, y + 25, textPaint);

        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(18);
        textPaint.setFakeBoldText(true);
        canvas.drawText(value, x + 15, y + 55, textPaint);
    }

    private static void drawTableHeader(Canvas canvas, int y, String[] headers) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(12);
        paint.setFakeBoldText(true);

        int[] xPositions = { 40, 200, 350, 480 }; // Adjust column widths

        for (int i = 0; i < headers.length; i++) {
            canvas.drawText(headers[i], xPositions[i], y, paint);
        }

        paint.setStrokeWidth(1);
        paint.setColor(Color.LTGRAY);
        canvas.drawLine(40, y + 5, 555, y + 5, paint);
    }

    private static void drawTableRow(Canvas canvas, int y, String[] values) {
        Paint paint = new Paint();
        paint.setColor(Color.DKGRAY);
        paint.setTextSize(11);
        paint.setFakeBoldText(false);

        int[] xPositions = { 40, 200, 350, 480 };

        for (int i = 0; i < values.length; i++) {
            if (values[i] != null)
                canvas.drawText(values[i], xPositions[i], y, paint);
        }
    }

    private static boolean checkPageBreak(int y) {
        return y > 780;
    }

    private static String formatCurrency(double amount) {
        return String.format(Locale.US, "%,.0f UGX", amount);
    }

    private static String truncate(String str, int len) {
        if (str == null)
            return "";
        if (str.length() > len)
            return str.substring(0, len) + "..";
        return str;
    }

    private static void shareFile(Context context, File file) {
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, "Share Report"));
    }
}
