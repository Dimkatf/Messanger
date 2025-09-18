package com.example.messager;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @GET("api/test")
    Call<String> testConnection();

    @POST("api/echo")
    Call<String> sendMessage(@Body String message);
}
