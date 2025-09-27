package com.example.messager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Screen_registration extends AppCompatActivity {
    private EditText phone_numberEdit, userNameEdit, passwordEdit;
    private Button backBtn, registrationBtn;
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

            if(phone_number.isEmpty() || userName.isEmpty() || password.isEmpty())
                toast("Заполните все поля!");
        });

    }
    private void toast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
