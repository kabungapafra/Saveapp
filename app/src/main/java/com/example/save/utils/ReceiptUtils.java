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
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.save.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReceiptUtils {

    // Brand palette (kept in sync with colors.xml)
    private static final int BRAND_BLUE = Color.parseColor("#215DA1");
    private static final int GREEN = Color.parseColor("#10B981");
    private static final int GREEN_BG = Color.parseColor("#E8F8F1");
    private static final int TEXT_DARK = Color.parseColor("#1A1A2E");
    private static final int TEXT_MUTED = Color.parseColor("#8A93A6");
    private static final int LINE = Color.parseColor("#E2E8F0");

    private static final int PAGE_W = 360;
    private static final int PAGE_H = 560;
    private static final int MARGIN = 28;

    public static void generateAndShareReceipt(Context context, String memberName, double amount, String type,
            Date date) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // --- Logo (centered) ---
        Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.save_logo);
        if (logo != null) {
            float logoW = 132f;
            float logoH = logoW * ((float) logo.getHeight() / logo.getWidth());
            float left = (PAGE_W - logoW) / 2f;
            float top = 36f;
            canvas.drawBitmap(logo, null, new RectF(left, top, left + logoW, top + logoH), paint);
        }

        // --- Title ---
        paint.setColor(BRAND_BLUE);
        paint.setTextSize(19);
        paint.setFakeBoldText(true);
        paint.setLetterSpacing(0.04f);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("PAYMENT RECEIPT", PAGE_W / 2f, 142, paint);

        paint.setColor(TEXT_MUTED);
        paint.setTextSize(10);
        paint.setFakeBoldText(false);
        paint.setLetterSpacing(0.02f);
        canvas.drawText("Save • Group Savings", PAGE_W / 2f, 160, paint);

        // --- Success badge ---
        paint.setColor(GREEN_BG);
        paint.setTextAlign(Paint.Align.LEFT);
        RectF badge = new RectF(PAGE_W / 2f - 56, 176, PAGE_W / 2f + 56, 202);
        canvas.drawRoundRect(badge, 13, 13, paint);
        paint.setColor(GREEN);
        paint.setTextSize(11);
        paint.setFakeBoldText(true);
        paint.setLetterSpacing(0.08f);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("SUCCESSFUL", PAGE_W / 2f, 193, paint);
        paint.setLetterSpacing(0f);

        // --- Details ---
        int y = 244;
        drawRow(canvas, paint, "Receipt ID", "#" + (System.currentTimeMillis() % 1_000_000_000L), y);
        y += 30;
        drawRow(canvas, paint, "Date",
                new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(date), y);
        y += 30;
        drawRow(canvas, paint, "Member", memberName != null ? memberName : "—", y);
        y += 30;
        drawRow(canvas, paint, "Payment Type", type != null ? type : "—", y);
        y += 26;

        // --- Amount highlight ---
        paint.setColor(GREEN_BG);
        RectF amountBox = new RectF(MARGIN, y, PAGE_W - MARGIN, y + 78);
        canvas.drawRoundRect(amountBox, 16, 16, paint);

        paint.setColor(TEXT_MUTED);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(11);
        paint.setFakeBoldText(true);
        paint.setLetterSpacing(0.1f);
        canvas.drawText("AMOUNT PAID", PAGE_W / 2f, y + 28, paint);

        paint.setColor(GREEN);
        paint.setTextSize(26);
        paint.setLetterSpacing(0f);
        canvas.drawText("UGX " + String.format(Locale.getDefault(), "%,.0f", amount), PAGE_W / 2f, y + 60, paint);

        // --- Footer ---
        paint.setColor(LINE);
        paint.setStrokeWidth(1);
        canvas.drawLine(MARGIN, PAGE_H - 70, PAGE_W - MARGIN, PAGE_H - 70, paint);

        paint.setColor(TEXT_DARK);
        paint.setTextSize(11);
        paint.setFakeBoldText(true);
        canvas.drawText("Thank you for your contribution!", PAGE_W / 2f, PAGE_H - 48, paint);

        paint.setColor(TEXT_MUTED);
        paint.setTextSize(9);
        paint.setFakeBoldText(false);
        canvas.drawText("This is an automatically generated receipt from Save.", PAGE_W / 2f, PAGE_H - 32, paint);

        document.finishPage(page);

        String fileName = "Receipt_" + System.currentTimeMillis() + ".pdf";
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
        try {
            document.writeTo(new FileOutputStream(file));
            document.close();
            shareFile(context, file);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error generating receipt", Toast.LENGTH_SHORT).show();
        }
    }

    /** Label on the left (muted), value right-aligned (dark bold). */
    private static void drawRow(Canvas canvas, Paint paint, String label, String value, int y) {
        paint.setFakeBoldText(false);
        paint.setLetterSpacing(0f);
        paint.setTextSize(12);
        paint.setColor(TEXT_MUTED);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(label, MARGIN, y, paint);

        paint.setColor(TEXT_DARK);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(value, PAGE_W - MARGIN, y, paint);
    }

    private static void shareFile(Context context, File file) {
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, "Share Receipt"));
    }
}
