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
 * RetrofitClient - PostgreSQL Edition
 * Configured to connect to the new PostgreSQL-backed API.
 */
public class RetrofitClient {
    // Replace this with your actual PostgreSQL Backend URL
    private static final String BASE_URL = "https://saveapp-backend-production-19fe.up.railway.app/api/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        String token = SessionManager.getInstance(context).getJwtToken();
                        if (token != null && !token.isEmpty()) {
                            Request request = original.newBuilder()
                                    .header("Authorization", "Bearer " + token)
                                    .header("Content-Type", "application/json")
                                    .build();
                            return chain.proceed(request);
                        }
                        return chain.proceed(original);
                    })
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}
