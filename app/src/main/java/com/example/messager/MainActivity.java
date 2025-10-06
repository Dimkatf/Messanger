package com.example.messager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private ApiService apiService;
    private Button registrBtn, loginBtn;
    private EditText numberPhoneEdit, passwordEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        apiService = ApiClient.getClient().create(ApiService.class);
        testConnection();

        numberPhoneEdit = findViewById(R.id.phoneEditMain);
        passwordEdit = findViewById(R.id.PasswordEditMain);

        registrBtn = findViewById(R.id.registrBtnMain);
        registrBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, Screen_registration.class);
            startActivity(intent);
        });

        loginBtn = findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(v -> {
            String numberphone = numberPhoneEdit.getText().toString();
            String password = passwordEdit.getText().toString();
            if(numberphone.isEmpty() || password.isEmpty())
                toast("Заполните все поля!");
            else {
               loginUser(numberphone, password);
            }
        });
    }


    private void loginUser(String phone, String password){
        try {
            String json = "{\"phone\":\"" + phone + "\",\"password\":\"" + password + "\"}";
            Log.d("LOGIN", "Sending: " + json);

            Call<String> call = apiService.loginUser(json);

            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    Log.d("LOGIN", "Response: " + response.body());

                    if(response.isSuccessful() && response.body() != null){
                        String result = response.body();

                        if(result.startsWith("LOGIN_SUCCESS")) {
                            // Парсим ответ: LOGIN_SUCCESS|ID|NAME|PHONE
                            String[] parts = result.split("\\|");
                            if(parts.length >= 4) {
                                Long userId = Long.parseLong(parts[1]);
                                String userName = parts[2];
                                String userPhone = parts[3];

                                toast("Добро пожаловать, " + userName + "!");

                                Intent intent = new Intent(MainActivity.this, MainScreen.class);
                                intent.putExtra("user_id", userId);
                                intent.putExtra("user_name", userName);
                                intent.putExtra("user_phone", userPhone);
                                startActivity(intent);

                                numberPhoneEdit.setText("");
                                passwordEdit.setText("");
                            }
                        } else {
                            toast("Неверный номер телефона или пароль");
                        }
                    } else {
                        toast("Ошибка сервера");
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e("LOGIN", "Error: " + t.getMessage());
                    toast("Ошибка соединения");
                }
            });
        } catch (Exception e) {
            Log.e("LOGIN", "Exception: " + e.getMessage());
            toast("Ошибка входа");
        }
    }

    private void saveUserData(String phone){
        getSharedPreferences("user_prefs", MODE_PRIVATE)
                .edit()
                .putString("user_phone", phone)
                .apply();
    }
    private void testConnection() {
        Call<String> call = apiService.testConnection();
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Log.d("API", "Success: " + response.body());
                    Toast.makeText(MainActivity.this, response.body(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("API", "Error: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void toast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
