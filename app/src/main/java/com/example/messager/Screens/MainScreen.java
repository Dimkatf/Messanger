package com.example.messager.Screens;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.messager.API.ApiService;
import com.example.messager.API.SessionManager;
import com.example.messager.Messages.Chats;
import com.example.messager.R;
import com.example.messager.Users.ChangeDataScreen;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainScreen extends AppCompatActivity {
    private Button chatsBtn;
    private TextView nameText;
    private Button changeBtn;
    private ImageView userPhotoView;
    private SessionManager sessionManager;
    private ApiService apiService;
    private static final String BASE_URL = "http://192.168.1.36:8080/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.main_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.screen_app), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Пожалуйста, войдите в систему", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        chatsBtn = findViewById(R.id.chatsMainScreen);
        changeBtn = findViewById(R.id.changeBtn);
        nameText = findViewById(R.id.name);
        userPhotoView = findViewById(R.id.imageView);

        String userName = sessionManager.getUserName();
        String userPhone = sessionManager.getUserPhone();
        Long userId = sessionManager.getUserId();

        nameText.setText(userName);

        if (userPhone != null && !userPhone.isEmpty()) {
            loadUserPhotoFromServer(userPhone);
        }

        chatsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, Chats.class);
            startActivity(intent);
        });

        changeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangeDataScreen.class);
            startActivityForResult(intent, 1);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            String newName = data.getStringExtra("new_name");
            if (newName != null) {
                nameText.setText(newName);
                sessionManager.createSession(
                        sessionManager.getUserId(),
                        newName,
                        sessionManager.getUserPhone()
                );
                toast("Имя обновлено!");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String savedName = sessionManager.getUserName();
        if (savedName != null && !savedName.isEmpty()) {
            nameText.setText(savedName);
        }

        String phone = sessionManager.getUserPhone();
        if (!phone.isEmpty()) {
            loadUserPhotoFromServer(phone);
        }
    }

    private void loadUserPhotoFromServer(String phone) {
        Call<ResponseBody> call = apiService.getUserPhoto(phone);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        byte[] photoBytes = response.body().bytes();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
                        userPhotoView.setImageBitmap(bitmap);
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

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}