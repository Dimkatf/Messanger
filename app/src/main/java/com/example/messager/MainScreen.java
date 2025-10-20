package com.example.messager;

import android.content.Intent;
import android.content.SharedPreferences;
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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainScreen extends AppCompatActivity {
    //private Button exitBtn;
    private Button chatsBtn;
    private Long userId;
    private String userName;
    private String userPhone;
    private TextView nameText;
    private Button changeBtn;
    private ImageView userPhotoView;
    private SharedPreferences prefs;
    private ApiService apiService;
    private static final String BASE_URL = "http://192.168.1.36:8080/";
    //private static final String BASE_URL = "http://10.0.2.2:8080/";

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

        chatsBtn = findViewById(R.id.chatsMainScreen);
        chatsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, Chats.class);
            startActivity(intent);
            finish();
        });
        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        //exitBtn = findViewById(R.id.exitByAccount);
        changeBtn = findViewById(R.id.changeBtn);
        nameText = findViewById(R.id.name);
        userPhotoView = findViewById(R.id.imageView);

        userId = getIntent().getLongExtra("user_id", -1);
        userName = getIntent().getStringExtra("user_name");
        userPhone = getIntent().getStringExtra("user_phone");

        if (userPhone != null && !userPhone.isEmpty()) {
            prefs.edit()
                    .putLong("user_id", userId)
                    .putString("user_name", userName)
                    .putString("user_phone", userPhone)
                    .apply();
        } else {
            userId = prefs.getLong("user_id", -1);
            userName = prefs.getString("user_name", "");
            userPhone = prefs.getString("user_phone", "");
        }

        nameText.setText(userName);

        if (userPhone != null && !userPhone.isEmpty()) {
            loadUserPhotoFromServer(userPhone);
        }
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
                userName = newName;
                toast("Имя обновлено!");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String savedName = prefs.getString("user_name", userName);
        if (savedName != null && !savedName.equals(userName)) {
            nameText.setText(savedName);
            userName = savedName;
        }

        String phone = prefs.getString("user_phone", "");
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

    private void loadUserMessages(Long id) {
    }

}
