package com.example.messager;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @GET("api/test")
    Call<String> testConnection();

    @POST("api/echo")
    Call<String> sendMessage(@Body String message);

    @GET("api/check-phone")
    Call<String> checkPhone(@Query("phone") String phone);

}
