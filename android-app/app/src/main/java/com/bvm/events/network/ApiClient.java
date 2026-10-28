package com.bvm.events.network;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.bvm.events.BuildConfig;
import com.bvm.events.util.SessionManager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static ApiClient instance;
    private final ApiService apiService;
    private final SessionManager sessionManager;
    private final Context appContext;

    private ApiClient(Context context) {
        this.appContext = context.getApplicationContext();
        this.sessionManager = new SessionManager(appContext);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS);

        // Auth Header Interceptor & 401 Session Handler
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder();

                String token = sessionManager.getToken();
                if (token != null && !token.trim().isEmpty()) {
                    requestBuilder.header("Authorization", "Bearer " + token);
                }

                Response response = chain.proceed(requestBuilder.build());

                // HTTP 401 Unauthorized handling
                if (response.code() == 401) {
                    sessionManager.clearSession();
                    handleUnauthorized();
                }

                return response;
            }
        });

        // Logging interceptor in debug builds
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(logging);
        }

        String baseUrl = BuildConfig.BASE_URL;
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        this.apiService = retrofit.create(ApiService.class);
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
    }

    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ApiClient not initialized. Call ApiClient.init(context) in Application class.");
        }
        return instance;
    }

    public ApiService getService() {
        return apiService;
    }

    private void handleUnauthorized() {
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                Class<?> loginClass = Class.forName("com.bvm.events.ui.LoginActivity");
                Intent intent = new Intent(appContext, loginClass);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                appContext.startActivity(intent);
            } catch (ClassNotFoundException ignored) {
                // LoginActivity may not be initialized yet in early foundation phase
            }
        });
    }
}

