package com.example.save.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReceiptUtils {

    public static void generateAndShareReceipt(Context context, String memberName, double amount, String type,
            Date date) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 500, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(14);
        paint.setFakeBoldText(true);

        // Header
        canvas.drawText("SAVE APP RECEIPT", 80, 50, paint);

        paint.setTextSize(12);
        paint.setFakeBoldText(false);
        canvas.drawText("Date: " + new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(date), 20, 90,
                paint);
        canvas.drawText("Receipt ID: " + System.currentTimeMillis(), 20, 110, paint);

        // Line
        paint.setStrokeWidth(1);
        canvas.drawLine(20, 120, 280, 120, paint);

        // Details
        canvas.drawText("Member: " + memberName, 20, 150, paint);
        canvas.drawText("Type: " + type, 20, 170, paint);

        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        canvas.drawText("Amount: UGX " + String.format(Locale.getDefault(), "%,.0f", amount), 20, 210, paint);

        // Footer
        paint.setTextSize(10);
        paint.setFakeBoldText(false);
        paint.setColor(Color.GRAY);
        canvas.drawText("Thank you for your contribution!", 60, 450, paint);

        document.finishPage(page);

        // Save
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

    private static void shareFile(Context context, File file) {
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, "Share Receipt"));
    }
}
