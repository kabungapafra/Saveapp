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
 *
 * IMPORTANT: Certificate pinning is intentionally disabled. When a server TLS cert
 * rotates (common with Cloudflare/Let's Encrypt), a stale pin causes OkHttp to hang
 * silently until the socket times out — producing SocketTimeoutException on the client.
 * Re-enable pinning only after verifying the current server certificate fingerprint.
 */
public class RetrofitClient {
    private static final String BASE_URL = "https://api.digiflecttech.dev/api/";
    private static RetrofitClient instance = null;
    private Retrofit retrofit;
    private OkHttpClient okHttpClient;
    // Keep a reference to the context to read fresh tokens on every request
    private final Context appContext;

    private RetrofitClient(Context context) {
        this.appContext = context.getApplicationContext();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        this.okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    // Always read the token fresh from the session manager so that
                    // login/logout token changes are reflected without recreating the client.
                    String token = SessionManager.getInstance(appContext).getJwtToken();
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

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    /**
     * @deprecated Token is now always read fresh from SessionManager per request.
     * This method is kept for backward compatibility but has no effect.
     */
    @Deprecated
    public synchronized void updateToken(String token) {
        // no-op: token is read fresh from SessionManager in the interceptor
    }

    /**
     * Cleanup on logout to prevent stale connections and in-flight request interference.
     */
    public void logout() {
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
