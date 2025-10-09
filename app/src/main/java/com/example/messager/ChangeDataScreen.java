package com.example.messager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ChangeDataScreen extends AppCompatActivity {
    private Button exit;
    private EditText changeNameText;
    private ImageView image;
    private String selectedImageUri;
    private Button changeBtn;
    private String userName;
    private ApiService apiService;
    private SharedPreferences prefs;
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    //private static final String BASE_URL = "http://192.168.1.36:8080/";

    private ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        selectedImageUri = imageUri.toString();
                        image.setImageURI(imageUri);
                        toast("Фото выбрано");
                        uploadPhotoToServer(imageUri);
                    }
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.change_datauser);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.change_screen), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);


        exit = findViewById(R.id.exitByChangeScreen);
        image = findViewById(R.id.selectedPhotoView);
        changeNameText = findViewById(R.id.changeNameBtn);
        changeBtn = findViewById(R.id.changeBtnInScreenChange);

        userName = prefs.getString("user_name", "");
        changeNameText.setText(userName);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create()) // ДОБАВЬТЕ ЭТУ СТРОЧКУ ПЕРВОЙ
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        String phone = prefs.getString("user_phone", "");
        if (!phone.isEmpty()) {
            loadPhotoFromServer(phone);
        }

        image.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        exit.setOnClickListener(v -> finish());

        changeBtn.setOnClickListener(v -> {
            String newName = changeNameText.getText().toString().trim();
            String userPhone = prefs.getString("user_phone", "");

            if(newName.isEmpty()) {
                toast("Введите имя");
                return;
            }

            ApiService.UpdateNameRequest request = new ApiService.UpdateNameRequest(userPhone, newName);

            apiService.updateUserName(request).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if(response.isSuccessful()) {
                        String responseBody = response.body();
                        System.out.println("✅ Success! Body: " + responseBody);

                        if (responseBody != null && responseBody.contains("\"status\":\"success\"")) {
                            prefs.edit().putString("user_name", newName).apply();
                            toast("Имя изменено!");

                            // ★★★★ ВОЗВРАЩАЕМ РЕЗУЛЬТАТ НА ГЛАВНЫЙ ЭКРАН ★★★★
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("new_name", newName);
                            setResult(RESULT_OK, resultIntent);
                        } else {
                            toast("Ошибка сервера");
                        }
                    } else {
                        System.out.println("❌ Server error: " + response.code());
                        try {
                            String errorBody = response.errorBody().string();
                            System.out.println("Error body: " + errorBody);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        toast("Ошибка сервера: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    toast("Ошибка сети: " + t.getMessage());
                }
            });
        });
    }

    private void uploadPhotoToServer(Uri imageUri) {
        try {
            String phone = prefs.getString("user_phone", "");
            if (phone.isEmpty()) {
                toast("Ошибка: телефон не найден");
                return;
            }

            System.out.println("📱 Starting photo upload for phone: " + phone);

            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            byte[] photoBytes = readBytes(inputStream);

            System.out.println("📱 Photo size: " + photoBytes.length + " bytes");

            RequestBody phoneBody = RequestBody.create(MediaType.parse("text/plain"), phone);
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), photoBytes);
            MultipartBody.Part photoPart = MultipartBody.Part.createFormData("photo", "photo.jpg", requestBody);

            System.out.println("📱 Sending request to server...");

            Call<ResponseBody> call = apiService.uploadPhoto(phoneBody, photoPart);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    System.out.println("📱 Server response code: " + response.code());
                    System.out.println("📱 Server response isSuccessful: " + response.isSuccessful());

                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseBody = response.body().string();
                            System.out.println("📱 Server response body: " + responseBody);

                            if (responseBody.contains("\"status\":\"success\"")) {
                                toast("Фото сохранено на сервере!");
                            } else {
                                toast("Ошибка сервера: " + responseBody);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            toast("Ошибка чтения ответа: " + e.getMessage());
                        }
                    } else {
                        try {
                            String errorBody = response.errorBody().string();
                            System.out.println("📱 Server error body: " + errorBody);
                            toast("Ошибка сервера: " + response.code() + " - " + errorBody);
                        } catch (Exception e) {
                            e.printStackTrace();
                            toast("Ошибка сервера: " + response.code());
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    System.out.println("📱 Network failure: " + t.getMessage());
                    t.printStackTrace();
                    toast("Ошибка сети при загрузке фото: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            toast("Ошибка обработки фото: " + e.getMessage());
        }
    }


    private void loadPhotoFromServer(String phone) {
        Call<ResponseBody> call = apiService.getUserPhoto(phone);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        byte[] photoBytes = response.body().bytes();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
                        image.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private byte[] readBytes(InputStream inputStream) throws java.io.IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void toast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}