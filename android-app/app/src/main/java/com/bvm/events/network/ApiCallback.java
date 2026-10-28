package com.bvm.events.network;

import com.bvm.events.model.ApiResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class ApiCallback<T> implements Callback<ApiResponse<T>> {

    private static final Gson GSON = new Gson();

    public abstract void onSuccess(T data);

    public abstract void onError(String message, String errorCode);

    @Override
    public void onResponse(Call<ApiResponse<T>> call, Response<ApiResponse<T>> response) {
        if (response.isSuccessful()) {
            ApiResponse<T> body = response.body();
            if (body != null && body.isSuccess()) {
                onSuccess(body.getData());
            } else {
                String msg = (body != null && !body.getMessage().isEmpty())
                        ? body.getMessage()
                        : "An unexpected error occurred.";
                String code = (body != null) ? body.getErrorCode() : "UNKNOWN_ERROR";
                onError(msg, code);
            }
        } else {
            // Parse error envelope from errorBody
            String message = "Server returned error: " + response.code();
            String errorCode = "HTTP_" + response.code();

            try {
                if (response.errorBody() != null) {
                    String errorJson = response.errorBody().string();
                    Type type = new TypeToken<ApiResponse<Object>>() {}.getType();
                    ApiResponse<?> parsed = GSON.fromJson(errorJson, type);
                    if (parsed != null) {
                        if (!parsed.getMessage().isEmpty()) {
                            message = parsed.getMessage();
                        }
                        if (!parsed.getErrorCode().isEmpty()) {
                            errorCode = parsed.getErrorCode();
                        }
                    }
                }
            } catch (Exception ignored) {
                // Keep default HTTP error message
            }

            onError(message, errorCode);
        }
    }

    @Override
    public void onFailure(Call<ApiResponse<T>> call, Throwable t) {
        if (t instanceof IOException) {
            onError("No internet or server unreachable", "NETWORK_ERROR");
        } else {
            onError(t.getMessage() != null ? t.getMessage() : "Unknown request failure", "CLIENT_ERROR");
        }
    }
}

