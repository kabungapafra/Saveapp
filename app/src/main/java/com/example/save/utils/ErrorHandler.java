package com.example.save.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.save.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ErrorHandler {

    public enum ErrorType {
        NETWORK,
        DATABASE,
        VALIDATION,
        PERMISSION,
        UNKNOWN
    }

    public interface ErrorAction {
        void onRetry();

        void onDismiss();
    }

    public static void showError(Context context, ErrorType type, String message, ErrorAction action) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_error, null);

        ImageView iconError = dialogView.findViewById(R.id.iconError);
        TextView tvErrorTitle = dialogView.findViewById(R.id.tvErrorTitle);
        TextView tvErrorMessage = dialogView.findViewById(R.id.tvErrorMessage);
        TextView tvErrorSolution = dialogView.findViewById(R.id.tvErrorSolution);

        // Set icon and title based on error type
        switch (type) {
            case NETWORK:
                iconError.setImageResource(R.drawable.ic_warning);
                tvErrorTitle.setText("Connection Error");
                tvErrorSolution.setText("Please check your internet connection and try again.");
                break;
            case DATABASE:
                iconError.setImageResource(R.drawable.ic_warning);
                tvErrorTitle.setText("Data Error");
                tvErrorSolution.setText("There was a problem accessing your data. Please try again.");
                break;
            case VALIDATION:
                iconError.setImageResource(R.drawable.ic_info);
                tvErrorTitle.setText("Invalid Input");
                tvErrorSolution.setText("Please check your input and try again.");
                break;
            case PERMISSION:
                iconError.setImageResource(R.drawable.ic_warning);
                tvErrorTitle.setText("Permission Required");
                tvErrorSolution.setText("Please grant the required permissions in Settings.");
                break;
            default:
                iconError.setImageResource(R.drawable.ic_warning);
                tvErrorTitle.setText("Something Went Wrong");
                tvErrorSolution.setText("An unexpected error occurred. Please try again.");
                break;
        }

        tvErrorMessage.setText(message);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setView(dialogView)
                .setCancelable(false);

        // Add retry button for network and database errors
        if (type == ErrorType.NETWORK || type == ErrorType.DATABASE) {
            builder.setPositiveButton("Retry", (dialog, which) -> {
                if (action != null) {
                    action.onRetry();
                }
            });
        }

        builder.setNegativeButton("Dismiss", (dialog, which) -> {
            if (action != null) {
                action.onDismiss();
            }
        });

        builder.show();
    }

    public static String getNetworkErrorMessage() {
        return "Unable to connect to the server. Please check your internet connection.";
    }

    public static String getDatabaseErrorMessage() {
        return "Failed to access local data. Please try again.";
    }

    public static String getValidationErrorMessage(String field) {
        return "Please enter a valid " + field + ".";
    }

    public static String getPermissionErrorMessage(String permission) {
        return "This feature requires " + permission + " permission.";
    }
}
