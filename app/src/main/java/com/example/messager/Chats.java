package com.example.messager;

import android.app.AlertDialog;
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
    private EditText searchEditText;
    private Button searchButton;
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

        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> searchUser());

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

        setupClickListeners();

        chatsRecyclerView.setAdapter(chatAdapter);

        loadLastMessageForFavorites();
    }

    private void setupClickListeners() {
        chatAdapter.setOnChatClickListener(chat -> {
            if (chat.getName().equals("Избранное")) {
                if (sessionManager.isLoggedIn()) {
                    Intent intent = new Intent(this, FavoritesScreen.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Пожалуйста, войдите в систему", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Открываем чат с " + chat.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        chatAdapter.setOnChatLongClickListener((chat, position) -> {
            showDeleteDialog(chat, position);
        });
    }

    private void showDeleteDialog(Chat chat, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Удалить чат")
                .setMessage("Вы уверены, что хотите удалить чат с " + chat.getName() + "?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    deleteChat(chat, position);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteChat(Chat chat, int position) {
        chatAdapter.removeChat(position);
        Toast.makeText(this, "Чат с " + chat.getName() + " удален", Toast.LENGTH_SHORT).show();

        // deleteChatFromServer(chat);
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

    private void searchUser() {
        String username = searchEditText.getText().toString().trim();
        if (username.isEmpty()) {
            Toast.makeText(this, "Введите имя пользователя", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<User> call = apiService.findUserByUsername(username);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User foundUser = response.body();
                    showFoundUserAsChat(foundUser);
                    searchEditText.setText("");
                } else {
                    Toast.makeText(Chats.this, "Пользователь не найден", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(Chats.this, "Ошибка поиска: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFoundUserAsChat(User user) {
        Chat userChat = new Chat(
                user.getUserName(),
                "Нажмите чтобы начать чат",
                "сейчас",
                false
        );

        chatAdapter.addChat(1, userChat);

        // chatAdapter.setOnChatClickListener(chat -> {
        //     if (chat.getChatName().equals(user.getUserName())) {
        //         createNewChatWithUser(user);
        //     }
        // });
    }

    // private void createNewChatWithUser(User user) {
    //     Toast.makeText(this, "Создаем чат с " + user.getUserName(), Toast.LENGTH_SHORT).show();
    //     Intent intent = new Intent(this, ChatActivity.class);
    //     intent.putExtra("user_id", user.getId());
    //     intent.putExtra("user_name", user.getUserName());
    //     startActivity(intent);
    // }

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