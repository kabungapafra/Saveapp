package com.example.save.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;

public class RetryHelper {

    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final long DEFAULT_INITIAL_DELAY = 1000; // 1 second

    public interface RetryCallback<T> {
        void onSuccess(T result);

        void onFailure(Exception error);
    }

    /**
     * Retry a callable operation with exponential backoff
     */
    public static <T> void retryWithBackoff(
            Callable<T> operation,
            RetryCallback<T> callback,
            int maxRetries,
            long initialDelay) {

        new Thread(() -> {
            int attempt = 0;
            long delay = initialDelay;
            Exception lastException = null;

            while (attempt < maxRetries) {
                try {
                    T result = operation.call();
                    // Success - notify on main thread
                    new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(result));
                    return;
                } catch (Exception e) {
                    lastException = e;
                    attempt++;

                    if (attempt < maxRetries) {
                        try {
                            Thread.sleep(delay);
                            delay *= 2; // Exponential backoff
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }

            // All retries failed - notify on main thread
            Exception finalException = lastException;
            new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(finalException));
        }).start();
    }

    /**
     * Retry with default settings
     */
    public static <T> void retry(Callable<T> operation, RetryCallback<T> callback) {
        retryWithBackoff(operation, callback, DEFAULT_MAX_RETRIES, DEFAULT_INITIAL_DELAY);
    }

    /**
     * Simple retry for operations that don't return a value
     */
    public static void retryOperation(
            Runnable operation,
            Runnable onSuccess,
            Runnable onFailure,
            int maxRetries) {

        new Thread(() -> {
            int attempt = 0;
            long delay = DEFAULT_INITIAL_DELAY;
            boolean success = false;

            while (attempt < maxRetries && !success) {
                try {
                    operation.run();
                    success = true;
                    new Handler(Looper.getMainLooper()).post(onSuccess);
                } catch (Exception e) {
                    attempt++;
                    if (attempt < maxRetries) {
                        try {
                            Thread.sleep(delay);
                            delay *= 2;
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }

            if (!success) {
                new Handler(Looper.getMainLooper()).post(onFailure);
            }
        }).start();
    }
}
