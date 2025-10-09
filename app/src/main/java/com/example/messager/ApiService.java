package com.example.messager;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {
    @GET("api/test")
    Call<String> testConnection();

    @POST("api/echo")
    Call<String> sendMessage(@Body String message);

    @GET("api/check-phone")
    Call<String> checkPhone(@Query("phone") String phone);

    @Headers("Content-Type: application/json")
    @POST("api/login")
    Call<String> loginUser(@Body String json);

    @PUT("api/update-name")
    Call<String> updateUserName(@Body UpdateNameRequest request);

    @Multipart
    @POST("api/upload-photo")
    Call<ResponseBody> uploadPhoto(@Part("phone") RequestBody phone,
                                   @Part MultipartBody.Part photo); // ResponseBody

    @GET("api/user-photo")
    Call<ResponseBody> getUserPhoto(@Query("phone") String phone);
    class UpdateNameRequest{
        private String phone;
        private String newName;
        public UpdateNameRequest(String phone, String newName) {
            this.phone = phone;
            this.newName = newName;
        }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getNewName() { return newName; }
        public void setNewName(String newName) { this.newName = newName; }
    }

}

