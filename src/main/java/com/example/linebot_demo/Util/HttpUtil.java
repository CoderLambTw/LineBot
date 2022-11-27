package com.example.linebot_demo.Util;

import okhttp3.*;

import java.io.IOException;

public class HttpUtil {
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();

    public String post(String url, String json) throws Exception {
        RequestBody body = RequestBody.create(json,JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
    public String get(String url) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}

