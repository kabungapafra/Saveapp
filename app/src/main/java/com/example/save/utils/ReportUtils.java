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

public class ReportUtils {

    public static void generateAndShareReport(Context context, List<Member> members, double totalSavings,
            double activeLoans) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 size
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);

        int y = 50;

        // Title
        paint.setTextSize(20);
        paint.setFakeBoldText(true);
        canvas.drawText("SAVE APP - GROUP REPORT", 180, y, paint);
        y += 40;

        // Date
        paint.setTextSize(12);
        paint.setFakeBoldText(false);
        canvas.drawText(
                "Generated on: " + new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(new Date()),
                50, y, paint);
        y += 30;

        // Summary
        paint.setTextSize(14);
        paint.setFakeBoldText(true);
        canvas.drawText("Summary", 50, y, paint);
        y += 20;

        paint.setTextSize(12);
        paint.setFakeBoldText(false);
        canvas.drawText("Total Group Savings: UGX " + String.format(Locale.US, "%,.0f", totalSavings), 50, y, paint);
        y += 20;
        canvas.drawText("Active Outstanding Loans: UGX " + String.format(Locale.US, "%,.0f", activeLoans), 50, y,
                paint);
        y += 30;

        // Member List Table Header
        paint.setFakeBoldText(true);
        canvas.drawText("Member Name", 50, y, paint);
        canvas.drawText("Savings", 250, y, paint);
        canvas.drawText("Status", 400, y, paint);

        paint.setStrokeWidth(1);
        canvas.drawLine(50, y + 5, 550, y + 5, paint);
        y += 25;

        // Member Data
        paint.setFakeBoldText(false);
        if (members != null) {
            for (Member member : members) {
                if (y > 750) { // New page if full
                    document.finishPage(page);
                    pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 2).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 50;
                }

                canvas.drawText(member.getName(), 50, y, paint);
                canvas.drawText(String.format(Locale.US, "%,.0f", member.getContributionPaid()), 250, y, paint);
                canvas.drawText(member.hasReceivedPayout() ? "Paid Out" : "Waiting", 400, y, paint);
                y += 20;
            }
        }

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

    private static void shareFile(Context context, File file) {
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, "Share Report"));
    }
}
