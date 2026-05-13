package com.example.save.data.network;

import android.content.Context;
import com.example.save.utils.SessionManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

/**
 * RetrofitClient - Singleton Edition
 * Configured for connection stability and efficient token management.
 */
public class RetrofitClient {
    private static final String BASE_URL = "https://api.digiflecttech.dev/api/";
    private static RetrofitClient instance = null;
    private Retrofit retrofit;
    private OkHttpClient okHttpClient;
    private String authToken;

    private RetrofitClient(Context context) {
        // Initialize token from session
        this.authToken = SessionManager.getInstance(context.getApplicationContext()).getJwtToken();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        okhttp3.CertificatePinner certPinner = new okhttp3.CertificatePinner.Builder()
                .add("api.digiflecttech.dev", "sha256/dDAb/Pkn0RnPn51pSazcWhSNGAR5ZV2lAxedGtLJG5I=")
                .build();

        this.okHttpClient = new OkHttpClient.Builder()
                .certificatePinner(certPinner)
                .addInterceptor(logging)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    String token = getAuthToken();
                    if (token != null && !token.isEmpty()) {
                        Request request = original.newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .header("Content-Type", "application/json")
                                .build();
                        return chain.proceed(request);
                    }
                    return chain.proceed(original);
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        this.retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }

    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) {
            instance = new RetrofitClient(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Legacy method for compatibility with existing code
     */
    public static Retrofit getClient(Context context) {
        return getInstance(context).getRetrofit();
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    public synchronized void updateToken(String token) {
        this.authToken = token;
    }

    private synchronized String getAuthToken() {
        return authToken;
    }

    /**
     * Cleanup on logout to prevent stale connections and in-flight request interference.
     */
    public void logout() {
        synchronized (this) {
            this.authToken = null;
        }
        if (okHttpClient != null) {
            // Cancel all in-flight requests
            okHttpClient.dispatcher().cancelAll();
            // Evict all connections from the pool
            okHttpClient.connectionPool().evictAll();
        }
    }

    /**
     * Create service helper
     */
    public <S> S createService(Class<S> serviceClass) {
        return retrofit.create(serviceClass);
    }
}
