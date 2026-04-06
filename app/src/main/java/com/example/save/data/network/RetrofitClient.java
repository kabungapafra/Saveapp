package com.example.save.data.network;

import android.content.Context;

import com.example.save.utils.SessionManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // Production backend URL hosted on Render
    private static final String DEFAULT_BASE_URL = "https://saveapp-backend.onrender.com/api/";
    private static final boolean USE_MOCK_DATA = true; // Set to false to use actual backend
    private static Retrofit retrofit = null;
    private static String currentBaseUrl = null;

    public static Retrofit getClient(Context context) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("ChamaPrefs", Context.MODE_PRIVATE);
        String baseUrl = prefs.getString("api_base_url", DEFAULT_BASE_URL);

        // Auto-fix: overwrite any old development URLs with the production URL
        if (baseUrl.contains("ngrok-free.app") || baseUrl.contains("192.168.1.44")
                || baseUrl.contains("save-app-backend.onrender.com")) {
            baseUrl = DEFAULT_BASE_URL;
            prefs.edit().putString("api_base_url", baseUrl).apply();
        }

        if (retrofit == null || !baseUrl.equals(currentBaseUrl)) {
            currentBaseUrl = baseUrl;
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

            if (android.util.Log.isLoggable("Retrofit", android.util.Log.DEBUG)) {
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            } else {
                logging.setLevel(HttpLoggingInterceptor.Level.NONE);
            }

            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        String token = SessionManager.getInstance(context).getJwtToken();

                        if (token != null && !token.isEmpty()) {
                            Request request = original.newBuilder()
                                    .header("Authorization", "Bearer " + token)
                                    .header("Content-Type", "application/json")
                                    .method(original.method(), original.body())
                                    .build();
                            return chain.proceed(request);
                        }
                        return chain.proceed(original);
                    })
                    .connectTimeout(90, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(90, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(90, java.util.concurrent.TimeUnit.SECONDS);

            if (USE_MOCK_DATA) {
                clientBuilder.addInterceptor(new MockDataInterceptor());
            }

            OkHttpClient client = clientBuilder.build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    /**
     * Update the API Base URL dynamically (e.g., from settings)
     */
    public static void updateBaseUrl(Context context, String newUrl) {
        if (newUrl != null && !newUrl.endsWith("/")) {
            newUrl += "/";
        }
        context.getSharedPreferences("ChamaPrefs", Context.MODE_PRIVATE)
                .edit()
                .putString("api_base_url", newUrl)
                .apply();
        // Reset retrofit instance to force recreation with new URL next time getClient
        // is called
        retrofit = null;
    }
}
