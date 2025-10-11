package com.example.messager;

import android.content.Intent;
import android.content.SharedPreferences;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private ApiService apiService;
    private Button registrBtn, loginBtn;
    private EditText numberPhoneEdit, passwordEdit;
    private SharedPreferences prefs;

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

        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        if(isUserLoggedIn()){
            goToMainScreen();
            return;
        }
        setContentView(R.layout.activity_main );
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
                            String[] parts = result.split("\\|");
                            if(parts.length >= 4) {
                                Long userId = Long.parseLong(parts[1]);
                                String userName = parts[2];
                                String userPhone = parts[3];

                                saveUserData(userId, userName, userPhone);
                                goToMainScreen();

                                toast("Добро пожаловать, " + userName + "!");

                                Intent intent = new Intent(MainActivity.this, MainScreen.class);
                                intent.putExtra("user_id", userId);
                                intent.putExtra("user_name", userName);
                                intent.putExtra("user_phone", phone);
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

    private void saveUserData(Long userId, String userName, String userPhone){
        getSharedPreferences("user_prefs", MODE_PRIVATE)
                .edit()
                .putLong("user_id", userId)
                .putString("user_name", userName)
                .putString("user_phone", userPhone)
                .apply();

        Log.d("SAVE", "✅ Saved to SharedPreferences:");
        Log.d("SAVE", "   User ID: " + userId);
        Log.d("SAVE", "   User Name: " + userName);
        Log.d("SAVE", "   User Phone: " + userPhone);
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
    private boolean isUserLoggedIn(){
        String userPhone = prefs.getString("user_phone", "");
        String userName = prefs.getString("user_name", "");
        return !userPhone.isEmpty() && !userName.isEmpty();
    }
    private void goToMainScreen(){
        Intent intent = new Intent(this, MainScreen.class);
        Long userId = prefs.getLong("user_id", - 1);
        String userPhone = prefs.getString("user_phone", "");
        String userName = prefs.getString("user_name", "");

        intent.putExtra("user_id", userId);
        intent.putExtra("user_name", userName);
        intent.putExtra("user_phone", userPhone);
        startActivity(intent);
        finish();


    }

    private void toast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}