package com.vbm.logger.sample.api;

import com.vbm.logger.NetworkLogger;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com/";

    private static volatile JsonPlaceholderApi api;

    private ApiClient() {
    }

    public static JsonPlaceholderApi getApi() {
        if (api == null) {
            synchronized (ApiClient.class) {
                if (api == null) {
                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(NetworkLogger.getInterceptor())
                            .build();

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    api = retrofit.create(JsonPlaceholderApi.class);
                }
            }
        }
        return api;
    }

    public static OkHttpClient getFailingClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(NetworkLogger.getInterceptor())
                .build();
    }
}
