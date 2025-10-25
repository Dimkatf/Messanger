package com.example.messager.Users;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.messager.API.ApiResponse;
import com.example.messager.API.ApiService;
import com.example.messager.Screens.MainActivity;
import com.example.messager.R;
import com.example.messager.API.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ChangeDataScreen extends AppCompatActivity {
    private Button exit;
    private EditText changeNameText;
    private ImageView image;
    private String selectedImageUri;
    private Button changeBtn;
    private String userName;
    private Button deleteAccount;
    private Button exitByAccount;
    private SessionManager sessionManager;
    private ApiService apiService;
    private EditText userNameText;
    private static final String BASE_URL = "http://192.168.1.36:8080/";

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
        setContentView(R.layout.change_datauser);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        sessionManager = new SessionManager(this);

        deleteAccount = findViewById(R.id.deleteAccount);
        exitByAccount = findViewById(R.id.exitByAccount);
        exit = findViewById(R.id.exitByChangeScreen);
        image = findViewById(R.id.selectedPhotoView);
        changeNameText = findViewById(R.id.changeNameBtn);
        changeBtn = findViewById(R.id.changeBtnInScreenChange);
        userNameText = findViewById(R.id.changeUsername);

        userName = sessionManager.getUserName();
        String userUsername = sessionManager.getUsername();

        changeNameText.setText(userName);

        if (userUsername != null && !userUsername.isEmpty()) {
            userNameText.setText(userUsername);
        } else {
            userNameText.setText("");
            userNameText.setHint("Введите username");
        }

        String phone = sessionManager.getUserPhone();
        if (!phone.isEmpty()) {
            loadPhotoFromServer(phone);
        }

        image.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        exit.setOnClickListener(v -> finish());

        deleteAccount.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });

        exitByAccount.setOnClickListener(v -> {
            logoutUser();
        });

        changeBtn.setOnClickListener(v -> {
            saveUserData();
        });
    }

    private void saveUserData() {
        String newName = changeNameText.getText().toString().trim();
        String newUsername = userNameText.getText().toString().trim();
        String userPhone = sessionManager.getUserPhone();

        if (newName.isEmpty()) {
            toast("Введите имя");
            return;
        }

        if (userPhone.isEmpty()) {
            toast("Ошибка: телефон не найден");
            return;
        }

        ApiService.UpdateNameRequest nameRequest = new ApiService.UpdateNameRequest(userPhone, newName);
        apiService.updateUserName(nameRequest).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    String responseBody = response.body();
                    System.out.println("✅ Name updated! Body: " + responseBody);

                    if (responseBody != null && responseBody.contains("\"status\":\"success\"")) {
                        updateUserNameInSession(newName);

                        if (!newUsername.isEmpty()) {
                            saveUsername(userPhone, newUsername);
                        } else {
                            toast("Данные успешно обновлены!");
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("new_name", newName);
                            setResult(RESULT_OK, resultIntent);
                        }
                    } else {
                        toast("Ошибка сервера при изменении имени");
                    }
                } else {
                    System.out.println("❌ Server error: " + response.code());
                    toast("Ошибка сервера при изменении имени: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                toast("Ошибка сети при изменении имени: " + t.getMessage());
            }
        });
    }

    private void updateUserNameInSession(String newName) {
        Long userId = sessionManager.getUserId();
        String userPhone = sessionManager.getUserPhone();
        String userUsername = sessionManager.getUsername();

        sessionManager.createSession(userId, newName, userPhone);
        sessionManager.saveUsername(userUsername);
    }

    private void saveUsername(String phone, String username) {
        String formattedUsername = username;
        if (!username.startsWith("@") && !username.isEmpty()) {
            formattedUsername = "@" + username;
        }

        System.out.println("📱 Original username: " + username);
        System.out.println("📱 Formatted username: " + formattedUsername);

        final String finalFormattedUsername = formattedUsername;

        AddUserName request = new AddUserName(phone, formattedUsername);

        apiService.addUserName(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    System.out.println("✅ Username updated! Status: " + apiResponse.getStatus());

                    if ("success".equals(apiResponse.getStatus())) {
                        sessionManager.saveUsername(finalFormattedUsername);
                        System.out.println("💾 Saved to SessionManager: " + finalFormattedUsername);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                userNameText.setText(finalFormattedUsername);
                                toast("Данные успешно обновлены!");
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                toast("Ошибка: " + apiResponse.getMessage());
                            }
                        });
                    }
                } else {
                    System.out.println("❌ Server error for username: " + response.code());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toast("Ошибка сервера: " + response.code());
                        }
                    });
                }

                Intent resultIntent = new Intent();
                resultIntent.putExtra("new_name", sessionManager.getUserName());
                setResult(RESULT_OK, resultIntent);
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toast("Ошибка сети при сохранении username: " + t.getMessage());
                    }
                });

                Intent resultIntent = new Intent();
                resultIntent.putExtra("new_name", sessionManager.getUserName());
                setResult(RESULT_OK, resultIntent);
            }
        });
    }

    private void uploadPhotoToServer(Uri imageUri) {
        try {
            // ИСПРАВЛЕНО: используем sessionManager вместо prefs
            String phone = sessionManager.getUserPhone();
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

    private void logoutUser() {
        sessionManager.logout();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void clearUserData() {
        sessionManager.logout();
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Удаление аккаунта")
                .setMessage("Вы уверены, что хотите удалить свой аккаунт? Это действие нельзя отменить.")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    deleteUserById();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteUserById() {
        Long userId = sessionManager.getUserId();
        if (userId == -1L) {
            toast("ID пользователя не найден");
            return;
        }
        Call<String> call = apiService.deleteUserById(userId);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body();
                    toast("Пользователь успешно удалён!");
                    clearUserData();
                    Intent intent = new Intent(ChangeDataScreen.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        toast("Ошибка: " + errorBody);
                    } catch (java.io.IOException e) {
                        toast("Ошибка сервера");
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                toast("Ошибка сети: " + t.getMessage());
            }
        });
    }
}