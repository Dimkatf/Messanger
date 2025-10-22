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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Chats extends AppCompatActivity {
    private Button profileBtn;
    private RecyclerView chatsRecyclerView;
    public ChatAdapter chatAdapter;
    private ApiService apiService;
    private SessionManager sessionManager;
    private static final String BASE_URL = "http://192.168.1.36:8080/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.chats);

        sessionManager = new SessionManager(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chat_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        profileBtn = findViewById(R.id.profile);
        profileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainScreen.class);
            startActivity(intent);
            finish();
        });

        chatsRecyclerView = findViewById(R.id.chatsRecyclerView);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Chat> chatList = new ArrayList<>();
        chatList.add(new Chat("Избранное", "Загрузка...", "", true));
        chatList.add(new Chat("Ярик", "Че, идем бухать?", "10:11", false));

        chatAdapter = new ChatAdapter(chatList, this);
        chatsRecyclerView.setAdapter(chatAdapter);

        loadLastMessageForFavorites();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLastMessageForFavorites();
    }

    public void updateFavoritesLastMessage(String newMessage, String timestamp) {
        if (chatAdapter != null) {
            chatAdapter.updateFavoritesLastMessage(newMessage, timestamp);
        }
    }

    private void loadLastMessageForFavorites() {
        String userId = sessionManager.getUserIdString();
        if (userId == null || userId.equals("-1")) {
            System.out.println("❌ User not logged in");
            updateFavoritesLastMessage("Войдите в систему", "");
            return;
        }

        String chatId = "favorites_" + userId;
        System.out.println("📱 Loading last message for: " + chatId);

        Call<Map<String, String>> call = apiService.getLastMessage(chatId);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                System.out.println("📱 Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    Map<String, String> apiResponse = response.body();
                    System.out.println("📱 Server response: " + apiResponse);

                    if ("success".equals(apiResponse.get("status"))) {
                        String text = apiResponse.get("message");
                        String timestamp = apiResponse.get("timestamp");

                        System.out.println("📱 Last message: " + text);
                        System.out.println("📱 Timestamp: " + timestamp);

                        updateFavoritesLastMessage(text, timestamp);
                    } else {
                        System.out.println("❌ Server returned error status");
                        updateFavoritesLastMessage("Нет сообщений", "");
                    }
                } else {
                    System.out.println("❌ Response not successful");
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "null";
                        System.out.println("❌ Error body: " + errorBody);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    updateFavoritesLastMessage("Ошибка загрузки", "");
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                System.out.println("❌ Network error: " + t.getMessage());
                updateFavoritesLastMessage("Ошибка сети", "");
            }
        });
    }
}