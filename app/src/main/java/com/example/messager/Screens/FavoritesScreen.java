package com.example.messager.Screens;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messager.API.ApiResponse;
import com.example.messager.API.ApiService;
import com.example.messager.API.SessionManager;
import com.example.messager.Messages.ChatMessage;
import com.example.messager.Messages.MessageAdapter;
import com.example.messager.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoritesScreen extends AppCompatActivity {
    private Button exitFavoritesBtn;
    private Button sendMessageBtn;
    private EditText editMessage;
    private RecyclerView messagesRecyclerView;
    private MessageAdapter messageAdapter;
    private List<ChatMessage> messageList = new ArrayList<>();
    private ApiService apiService;
    private SessionManager sessionManager;
    private static final String BASE_URL = "http://192.168.1.36:8080/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites_activity);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Пожалуйста, войдите в систему", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String currentUserId = sessionManager.getUserIdString();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        exitFavoritesBtn = findViewById(R.id.backFavorites);
        sendMessageBtn = findViewById(R.id.sendMessageFavorites);
        editMessage = findViewById(R.id.messageForFavoritesEdit);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);

        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        messageAdapter = new MessageAdapter(messageList, this, apiService, currentUserId);
        messagesRecyclerView.setAdapter(messageAdapter);

        exitFavoritesBtn.setOnClickListener(v -> finish());

        sendMessageBtn.setOnClickListener(v -> {
            String text = editMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessageToServer(text, currentUserId);
                editMessage.setText("");
            }
        });

        loadAllMessages(currentUserId);
    }

    private void sendMessageToServer(String text, String userId) {
        Map<String, String> request = new HashMap<>();
        request.put("chatId", "favorites_" + userId);
        request.put("text", text);
        request.put("userId", userId);

        Call<ApiResponse> call = apiService.sendMessage(request);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if ("success".equals(apiResponse.getStatus())) {
                        loadAllMessages(userId);
                        Toast.makeText(FavoritesScreen.this, "Сообщение отправлено", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(FavoritesScreen.this, "Ошибка: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(FavoritesScreen.this, "Ошибка сервера: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(FavoritesScreen.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAllMessages(String userId) {
        Call<List<ChatMessage>> call = apiService.getMessages("favorites_" + userId);
        call.enqueue(new Callback<List<ChatMessage>>() {
            @Override
            public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    messageList.clear();
                    messageList.addAll(response.body());
                    messageAdapter.notifyDataSetChanged();

                    if (!messageList.isEmpty()) {
                        messagesRecyclerView.scrollToPosition(messageList.size() - 1);
                    }
                } else {
                    Toast.makeText(FavoritesScreen.this, "Ошибка сервера: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ChatMessage>> call, Throwable t) {
                Toast.makeText(FavoritesScreen.this, "Ошибка загрузки: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteMessage(Long messageId, int position) {
        Call<ApiResponse> call = apiService.deleteMessage(messageId);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if ("success".equals(apiResponse.getStatus())) {
                        runOnUiThread(() -> {
                            messageList.remove(position);
                            messageAdapter.notifyItemRemoved(position);
                            messageAdapter.notifyItemRangeChanged(position, messageList.size() - position);
                            Toast.makeText(FavoritesScreen.this, "Сообщение удалено", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(FavoritesScreen.this, "Ошибка: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(FavoritesScreen.this, "Ошибка сервера: " + response.code(), Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                runOnUiThread(() ->
                        Toast.makeText(FavoritesScreen.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}