package com.example.save.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.widget.Toast;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.save.R;
import com.example.save.data.models.Member;
import com.example.save.data.models.ComprehensiveReportResponse;

public class ReportUtils {

    // Brand palette (kept in sync with colors.xml)
    private static final int BRAND_BLUE = Color.parseColor("#215DA1");
    private static final int ORANGE = Color.parseColor("#FF8A00");
    private static final int TEXT_DARK = Color.parseColor("#1A1A2E");
    private static final int TEXT_MUTED = Color.parseColor("#616161");
    private static final String BRAND_HEX = "#215DA1";

    // ════════════════════════════════════════════════════════════════════════
    //  PDF EXPORT
    // ════════════════════════════════════════════════════════════════════════

    public static void generateAndShareReport(Context context, ComprehensiveReportResponse report) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        int y;
        int pageNumber = 1;

        // --- Branded Header ---
        Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.save_logo);
        if (logo != null) {
            float logoW = 120f;
            float logoH = logoW * ((float) logo.getHeight() / logo.getWidth());
            canvas.drawBitmap(logo, null, new RectF(40, 30, 40 + logoW, 30 + logoH), paint);
        }

        // Group name + timestamp (right-aligned)
        paint.setColor(BRAND_BLUE);
        paint.setTextSize(15);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.RIGHT);
        String groupName = report.getGroupName() != null ? report.getGroupName().toUpperCase(Locale.getDefault()) : "YOUR GROUP";
        canvas.drawText(groupName, 555, 52, paint);

        paint.setColor(TEXT_MUTED);
        paint.setTextSize(9);
        paint.setFakeBoldText(false);
        canvas.drawText("Generated " + new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(new Date()),
                555, 70, paint);
        paint.setTextAlign(Paint.Align.LEFT);

        // Brand accent rule
        paint.setColor(BRAND_BLUE);
        paint.setStrokeWidth(3);
        canvas.drawLine(40, 100, 555, 100, paint);

        // Report title
        paint.setColor(TEXT_DARK);
        paint.setTextSize(20);
        paint.setFakeBoldText(true);
        canvas.drawText("Group Financial Report", 40, 132, paint);

        y = 170;

        // --- Executive Summary ---
        paint.setColor(TEXT_DARK);
        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        canvas.drawText("EXECUTIVE SUMMARY", 40, y, paint);
        y += 30;

        int boxWidth = 240, boxHeight = 70, gap = 30;
        drawSummaryBox(canvas, "Total Balance", formatCurrency(report.getTotalBalance()), 40, y, boxWidth, boxHeight, "#E8F0FA");
        drawSummaryBox(canvas, "Total Contributions", formatCurrency(report.getTotalContributions()), 40 + boxWidth + gap, y, boxWidth, boxHeight, "#E8F8F1");
        y += boxHeight + 20;
        drawSummaryBox(canvas, "Active Loans (" + report.getActiveLoansCount() + ")", formatCurrency(report.getActiveLoansAmount()), 40, y, boxWidth, boxHeight, "#FFF3E0");
        drawSummaryBox(canvas, "Total Payouts", formatCurrency(report.getTotalPayoutsAmount()), 40 + boxWidth + gap, y, boxWidth, boxHeight, "#FCE4EC");
        y += boxHeight + 40;

        // --- Group Statistics ---
        paint.setColor(TEXT_DARK);
        paint.setTextSize(14);
        paint.setFakeBoldText(true);
        canvas.drawText("GROUP STATISTICS", 40, y, paint);
        y += 20;
        paint.setColor(TEXT_DARK);
        paint.setTextSize(12);
        paint.setFakeBoldText(false);
        canvas.drawText("• Total Members: " + report.getTotalMembers(), 50, y, paint);
        y += 20;
        canvas.drawText("• Active Loan Volume: " + formatCurrency(report.getActiveLoansAmount()), 50, y, paint);
        y += 20;
        canvas.drawText("• Net Financial Position: " + formatCurrency(report.getTotalBalance() + report.getActiveLoansAmount()), 50, y, paint);
        y += 40;

        // --- Active Loans Table ---
        if (checkPageBreak(y)) { document.finishPage(page); pageInfo = new PdfDocument.PageInfo.Builder(595, 842, ++pageNumber).create(); page = document.startPage(pageInfo); canvas = page.getCanvas(); y = 50; }
        drawSectionHeader(canvas, "ACTIVE LOANS", y);
        y += 30;
        drawTableHeader(canvas, y, new String[]{"Member", "Amount", "Balance", "Due Date"});
        y += 25;
        if (report.getActiveLoans() != null && !report.getActiveLoans().isEmpty()) {
            for (com.example.save.data.models.Loan loan : report.getActiveLoans()) {
                if (checkPageBreak(y)) { document.finishPage(page); pageInfo = new PdfDocument.PageInfo.Builder(595, 842, ++pageNumber).create(); page = document.startPage(pageInfo); canvas = page.getCanvas(); y = 50; drawTableHeader(canvas, y, new String[]{"Member", "Amount", "Balance", "Due Date"}); y += 25; }
                double balance = loan.getTotalDue() - loan.getRepaidAmount();
                String dueStr = loan.getDueDate() != null ? new SimpleDateFormat("dd MMM", Locale.getDefault()).format(loan.getDueDate()) : "-";
                drawTableRow(canvas, y, new String[]{truncate(loan.getMemberName(), 18), formatCurrency(loan.getAmount()), formatCurrency(balance), dueStr});
                y += 20;
            }
        } else {
            drawTableRow(canvas, y, new String[]{"No active loans", "-", "-", "-"});
            y += 20;
        }
        y += 40;

        // --- Payouts Table ---
        if (checkPageBreak(y)) { document.finishPage(page); pageInfo = new PdfDocument.PageInfo.Builder(595, 842, ++pageNumber).create(); page = document.startPage(pageInfo); canvas = page.getCanvas(); y = 50; }
        drawSectionHeader(canvas, "RECENT PAYOUTS", y);
        y += 30;
        drawTableHeader(canvas, y, new String[]{"Member", "Amount", "Desc", "Date"});
        y += 25;
        if (report.getPayouts() != null && !report.getPayouts().isEmpty()) {
            for (com.example.save.data.models.TransactionEntity tx : report.getPayouts()) {
                if (checkPageBreak(y)) { document.finishPage(page); pageInfo = new PdfDocument.PageInfo.Builder(595, 842, ++pageNumber).create(); page = document.startPage(pageInfo); canvas = page.getCanvas(); y = 50; drawTableHeader(canvas, y, new String[]{"Member", "Amount", "Desc", "Date"}); y += 25; }
                String dateTx = tx.getDate() != null ? new SimpleDateFormat("dd MMM", Locale.getDefault()).format(tx.getDate()) : "-";
                String memberDisplay = tx.getMemberName() != null ? truncate(tx.getMemberName(), 15) : "Unknown";
                drawTableRow(canvas, y, new String[]{memberDisplay, formatCurrency(tx.getAmount()), truncate(tx.getDescription(), 20), dateTx});
                y += 20;
            }
        } else {
            drawTableRow(canvas, y, new String[]{"No recent payouts", "-", "-", "-"});
            y += 20;
        }
        y += 40;

        // --- Member Directory ---
        if (checkPageBreak(y)) { document.finishPage(page); pageInfo = new PdfDocument.PageInfo.Builder(595, 842, ++pageNumber).create(); page = document.startPage(pageInfo); canvas = page.getCanvas(); y = 50; }
        drawSectionHeader(canvas, "MEMBER DIRECTORY", y);
        y += 30;
        drawTableHeader(canvas, y, new String[]{"Name", "Contact", "Paid", "Status"});
        y += 25;
        if (report.getMembers() != null) {
            for (Member member : report.getMembers()) {
                if (checkPageBreak(y)) { document.finishPage(page); pageInfo = new PdfDocument.PageInfo.Builder(595, 842, ++pageNumber).create(); page = document.startPage(pageInfo); canvas = page.getCanvas(); y = 50; drawTableHeader(canvas, y, new String[]{"Name", "Contact", "Paid", "Status"}); y += 25; }
                String status = member.hasReceivedPayout() ? "Paid Out" : (member.isActive() ? "Active" : "Inactive");
                drawTableRow(canvas, y, new String[]{truncate(member.getName(), 18), member.getPhone(), formatCurrency(member.getContributionPaid()), status});
                y += 20;
            }
        }

        // --- Footer ---
        paint.setColor(Color.LTGRAY);
        paint.setTextSize(10);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Generated by Save • End of Report", 297, 820, paint);
        paint.setTextAlign(Paint.Align.LEFT);

        document.finishPage(page);

        String fileName = "Group_Report_" + System.currentTimeMillis() + ".pdf";
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
        try {
            document.writeTo(new FileOutputStream(file));
            document.close();
            shareFile(context, file, "application/pdf", "Share Report");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error generating report", Toast.LENGTH_SHORT).show();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  EXCEL EXPORT  (HTML-table .xls — opens in Excel / Google Sheets, logo embedded)
    // ════════════════════════════════════════════════════════════════════════

    public static void generateAndShareExcel(Context context, ComprehensiveReportResponse report) {
        String logoTag = buildLogoImgTag(context);
        String groupName = report.getGroupName() != null ? report.getGroupName() : "Your Group";
        String generated = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(new Date());

        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta charset=\"UTF-8\"></head><body style=\"font-family:Calibri,Arial,sans-serif;\">");

        // Header
        sb.append("<table cellpadding=\"6\" style=\"width:760px;\"><tr>")
          .append("<td style=\"width:170px;\">").append(logoTag).append("</td>")
          .append("<td style=\"text-align:right;\">")
          .append("<div style=\"font-size:18px;font-weight:bold;color:").append(BRAND_HEX).append(";\">")
          .append(esc(groupName.toUpperCase(Locale.getDefault()))).append("</div>")
          .append("<div style=\"font-size:11px;color:#616161;\">Group Financial Report</div>")
          .append("<div style=\"font-size:10px;color:#8A93A6;\">Generated ").append(esc(generated)).append("</div>")
          .append("</td></tr></table>");

        // Executive summary
        sb.append(sectionTitle("Executive Summary"));
        sb.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"8\" style=\"border-collapse:collapse;width:760px;\">");
        sb.append(kvRow("Total Balance", formatCurrency(report.getTotalBalance())));
        sb.append(kvRow("Total Contributions", formatCurrency(report.getTotalContributions())));
        sb.append(kvRow("Active Loans (" + report.getActiveLoansCount() + ")", formatCurrency(report.getActiveLoansAmount())));
        sb.append(kvRow("Total Payouts", formatCurrency(report.getTotalPayoutsAmount())));
        sb.append(kvRow("Total Members", String.valueOf(report.getTotalMembers())));
        sb.append(kvRow("Net Financial Position", formatCurrency(report.getTotalBalance() + report.getActiveLoansAmount())));
        sb.append("</table>");

        // Active loans
        sb.append(sectionTitle("Active Loans"));
        sb.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"8\" style=\"border-collapse:collapse;width:760px;\">");
        sb.append(headerRow("Member", "Amount", "Balance", "Due Date"));
        if (report.getActiveLoans() != null && !report.getActiveLoans().isEmpty()) {
            for (com.example.save.data.models.Loan loan : report.getActiveLoans()) {
                double balance = loan.getTotalDue() - loan.getRepaidAmount();
                String due = loan.getDueDate() != null ? new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(loan.getDueDate()) : "-";
                sb.append(dataRow(loan.getMemberName(), formatCurrency(loan.getAmount()), formatCurrency(balance), due));
            }
        } else {
            sb.append(dataRow("No active loans", "-", "-", "-"));
        }
        sb.append("</table>");

        // Payouts
        sb.append(sectionTitle("Recent Payouts"));
        sb.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"8\" style=\"border-collapse:collapse;width:760px;\">");
        sb.append(headerRow("Member", "Amount", "Description", "Date"));
        if (report.getPayouts() != null && !report.getPayouts().isEmpty()) {
            for (com.example.save.data.models.TransactionEntity tx : report.getPayouts()) {
                String date = tx.getDate() != null ? new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(tx.getDate()) : "-";
                sb.append(dataRow(tx.getMemberName() != null ? tx.getMemberName() : "Unknown", formatCurrency(tx.getAmount()), tx.getDescription(), date));
            }
        } else {
            sb.append(dataRow("No recent payouts", "-", "-", "-"));
        }
        sb.append("</table>");

        // Member directory
        sb.append(sectionTitle("Member Directory"));
        sb.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"8\" style=\"border-collapse:collapse;width:760px;\">");
        sb.append(headerRow("Name", "Contact", "Contributed", "Status"));
        if (report.getMembers() != null) {
            for (Member m : report.getMembers()) {
                String status = m.hasReceivedPayout() ? "Paid Out" : (m.isActive() ? "Active" : "Inactive");
                sb.append(dataRow(m.getName(), m.getPhone(), formatCurrency(m.getContributionPaid()), status));
            }
        }
        sb.append("</table>");

        sb.append("<p style=\"font-size:10px;color:#8A93A6;margin-top:14px;\">Generated by Save • End of Report</p>");
        sb.append("</body></html>");

        String fileName = "Group_Report_" + System.currentTimeMillis() + ".xls";
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(sb.toString().getBytes("UTF-8"));
            shareFile(context, file, "application/vnd.ms-excel", "Share Excel Report");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error generating Excel report", Toast.LENGTH_SHORT).show();
        }
    }

    // --- Excel HTML helpers ---

    private static String buildLogoImgTag(Context context) {
        try (InputStream is = context.getAssets().open("save_logo.png")) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = is.read(buf)) > 0) baos.write(buf, 0, n);
            String b64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
            return "<img src=\"data:image/png;base64," + b64 + "\" width=\"150\" alt=\"Save\"/>";
        } catch (IOException e) {
            return "<div style=\"font-size:20px;font-weight:bold;color:" + BRAND_HEX + ";\">Save</div>";
        }
    }

    private static String sectionTitle(String t) {
        return "<h3 style=\"color:" + BRAND_HEX + ";margin:16px 0 6px 0;\">" + esc(t) + "</h3>";
    }

    private static String headerRow(String... cells) {
        StringBuilder r = new StringBuilder("<tr style=\"background:" + BRAND_HEX + ";color:#FFFFFF;font-weight:bold;\">");
        for (String c : cells) r.append("<td>").append(esc(c)).append("</td>");
        return r.append("</tr>").toString();
    }

    private static String dataRow(String... cells) {
        StringBuilder r = new StringBuilder("<tr>");
        for (String c : cells) r.append("<td style=\"color:#333333;\">").append(esc(c)).append("</td>");
        return r.append("</tr>").toString();
    }

    private static String kvRow(String k, String v) {
        return "<tr><td style=\"background:#F1F5F9;font-weight:bold;width:280px;\">" + esc(k)
                + "</td><td>" + esc(v) + "</td></tr>";
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // --- PDF helpers ---

    private static void drawSectionHeader(Canvas canvas, String title, int y) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(BRAND_BLUE);
        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        canvas.drawText(title, 40, y, paint);
        paint.setStrokeWidth(2);
        canvas.drawLine(40, y + 5, 555, y + 5, paint);
    }

    private static void drawSummaryBox(Canvas canvas, String label, String value, int x, int y, int width, int height, String colorHex) {
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor(colorHex));
        bgPaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(new RectF(x, y, x + width, y + height), 10, 10, bgPaint);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#616161"));
        textPaint.setTextSize(12);
        canvas.drawText(label, x + 15, y + 25, textPaint);

        textPaint.setColor(TEXT_DARK);
        textPaint.setTextSize(18);
        textPaint.setFakeBoldText(true);
        canvas.drawText(value, x + 15, y + 55, textPaint);
    }

    private static void drawTableHeader(Canvas canvas, int y, String[] headers) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(TEXT_DARK);
        paint.setTextSize(12);
        paint.setFakeBoldText(true);
        int[] xPositions = {40, 200, 350, 480};
        for (int i = 0; i < headers.length; i++) canvas.drawText(headers[i], xPositions[i], y, paint);
        paint.setStrokeWidth(1);
        paint.setColor(Color.LTGRAY);
        canvas.drawLine(40, y + 5, 555, y + 5, paint);
    }

    private static void drawTableRow(Canvas canvas, int y, String[] values) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.DKGRAY);
        paint.setTextSize(11);
        paint.setFakeBoldText(false);
        int[] xPositions = {40, 200, 350, 480};
        for (int i = 0; i < values.length; i++) if (values[i] != null) canvas.drawText(values[i], xPositions[i], y, paint);
    }

    private static boolean checkPageBreak(int y) {
        return y > 780;
    }

    private static String formatCurrency(double amount) {
        return String.format(Locale.US, "%,.0f UGX", amount);
    }

    private static String truncate(String str, int len) {
        if (str == null) return "";
        if (str.length() > len) return str.substring(0, len) + "..";
        return str;
    }

    private static void shareFile(Context context, File file, String mime, String chooserTitle) {
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(mime);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, chooserTitle));
    }
}
