package com.example.save.data.network;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MockDataInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        String uri = chain.request().url().uri().toString();

        String responseString = "";
        int responseCode = 200;

        if (uri.contains("auth/login")) {
            responseString = MockDataStore.LOGIN_SUCCESS;
        } else if (uri.contains("analytics/dashboard") || uri.contains("analytics/summary")) {
            responseString = MockDataStore.DASHBOARD_DATA;
        } else if (uri.contains("members")) {
            responseString = MockDataStore.MEMBERS_LIST;
        } else if (uri.contains("loans")) {
            responseString = MockDataStore.LOANS_LIST;
        } else if (uri.contains("config")) {
            responseString = MockDataStore.SYSTEM_CONFIG;
        } else {
            responseString = MockDataStore.API_SUCCESS;
        }

        return new Response.Builder()
                .code(responseCode)
                .message("OK (Mock Mode)")
                .request(chain.request())
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create(
                        MediaType.parse("application/json"),
                        responseString.getBytes()))
                .addHeader("content-type", "application/json")
                .build();
    }
}
