package com.example.messager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Screen_registration extends AppCompatActivity {
    private EditText phone_numberEdit, userNameEdit, passwordEdit;
    private Button backBtn, registrationBtn;

    //private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static final String BASE_URL = "http://192.168.1.36:8080/";

    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.screen_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.screen_reg), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        phone_numberEdit = findViewById(R.id.editTextPhone);
        userNameEdit = findViewById(R.id.editTextUsername);
        passwordEdit = findViewById(R.id.editTextPassword);

        backBtn = findViewById(R.id.back_registrationBtn);
        backBtn.setOnClickListener(v -> {
            finish();
        });

        registrationBtn = findViewById(R.id.create_userBtn);
        registrationBtn.setOnClickListener(v -> {
            String phone_number = phone_numberEdit.getText().toString();
            String userName = userNameEdit.getText().toString();
            String password = passwordEdit.getText().toString();

            if(phone_number.isEmpty() || userName.isEmpty() || password.isEmpty()) {
                toast("Заполните все поля!");
            } else {
                if(!phone_number.startsWith("8")){
                    toast("Номер телефона должен начинаться с '8'!");
                    return;
                }
                if(phone_number.length() != 11){
                    toast("Номер телефона должен содержать 11 цифр!");
                    return;
                }
                if(!phone_number.matches("\\d+")){
                    toast("Номер телефона должен содержать только цифры!");
                    return;
                }
                if(password.length() < 6){
                    toast("Пароль должен содержать не меньше 6 символов!");
                    return;
                }

                checkPhoneNumberOnServer(phone_number, userName, password);
            }
        });
    }

    private void checkPhoneNumberOnServer(String phoneNumber, String userName, String password) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<String> call = apiService.checkPhone(phoneNumber);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String result = response.body();
                    if ("EXISTS".equals(result)) {
                        toast("Пользователь с таким номером телефона уже зарегистрирован!");
                    } else {
                        registerUser(userName, phoneNumber, password);
                    }
                } else {
                    toast("Ошибка при проверке номера телефона");
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                toast("Ошибка соединения: " + t.getMessage());
            }
        });
    }

    private void registerUser(String name, String phone, String password){
        try{
            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("phone", phone);
            json.put("password", password);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(BASE_URL + "api/register")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    runOnUiThread(() -> toast("Ошибка подключения: " + e.getMessage()));
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    String responseBody = response.body().string();
                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            toast("Пользователь успешно создан");
                            finish();
                        } else {
                            toast("Ошибка: " + responseBody);
                        }
                    });
                }
            });
        } catch (Exception e){
            toast("Ошибка: " + e.getMessage());
        }
    }

    private void toast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}