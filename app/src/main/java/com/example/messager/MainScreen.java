package com.example.messager;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainScreen extends AppCompatActivity {
    private Button exitBtn;
    private Long userId;
    private String userName;
    private String userPhone;
    private TextView nameText;
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
        exitBtn = findViewById(R.id.exitByAccount);
        exitBtn.setOnClickListener(v -> {finish();});

        userId = getIntent().getLongExtra("user_id", -1);
        userName = getIntent().getStringExtra("user_name");
        userPhone = getIntent().getStringExtra("user_phone");

        nameText = findViewById(R.id.name);
        nameText.setText(userName);



    }
    private void loadUserMessages(Long id){}
}
