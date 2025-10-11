package com.example.messager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Chats extends AppCompatActivity {
    private Button profileBtn;
    private RecyclerView chatsRecyclerView;
    private ChatAdapter chatAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.chats);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chat_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        profileBtn = findViewById(R.id.profile);
        profileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainScreen.class);
            startActivity(intent);
            finish();
        });
        chatsRecyclerView = findViewById(R.id.chatsRecyclerView);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Chat> chatList = new ArrayList<>();
        chatList.add(new Chat("Избранное", "Важные контакты", ""));
        chatList.add(new Chat("Ярик", "Че, идем бухать?", "10:11"));

        chatAdapter = new ChatAdapter(chatList);
        chatsRecyclerView.setAdapter(chatAdapter);

    }
}
