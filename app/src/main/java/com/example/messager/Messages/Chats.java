package com.example.messager.Messages;

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

import com.example.messager.API.ApiService;
import com.example.messager.Screens.FavoritesScreen;
import com.example.messager.Screens.MainScreen;
import com.example.messager.R;
import com.example.messager.API.SessionManager;
import com.example.messager.Users.User;

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

        chatAdapter = new ChatAdapter(chatList, this);
        setupClickListeners();
        chatsRecyclerView.setAdapter(chatAdapter);

        loadUserChatsFromServer();
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
        if (chat.getName().equals("Избранное")) {
            Toast.makeText(this, "Нельзя удалить Избранное", Toast.LENGTH_SHORT).show();
            return;
        }

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

        if (isChatAlreadyExists(username)) {
            Toast.makeText(this, "Чат с пользователем " + username + " уже существует", Toast.LENGTH_SHORT).show();
            searchEditText.setText("");
            return;
        }

        Call<User> call = apiService.findUserByUsername(username);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User foundUser = response.body();
                    createChatOnServer(foundUser);
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

    private boolean isChatAlreadyExists(String username) {
        for (Chat chat : chatAdapter.chatList) {
            if (chat.getName().equals(username)) {
                return true;
            }
        }
        return false;
    }

    private void createChatOnServer(User foundUser) {
        String currentUserId = sessionManager.getUserIdString();
        if (currentUserId == null || currentUserId.equals("-1")) {
            Toast.makeText(this, "Войдите в систему для создания чата", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<Chat> call = apiService.createChat(Long.parseLong(currentUserId), foundUser.getId());
        call.enqueue(new Callback<Chat>() {
            @Override
            public void onResponse(Call<Chat> call, Response<Chat> response) {
                if (response.isSuccessful() && response.body() != null) {
                    loadUserChatsFromServer();
                    Toast.makeText(Chats.this, "Чат с " + foundUser.getUserName() + " создан", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Chats.this, "Ошибка создания чата на сервере", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Chat> call, Throwable t) {
                Toast.makeText(Chats.this, "Ошибка сети при создании чата: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserChatsFromServer() {
        String userId = sessionManager.getUserIdString();
        if (userId == null || userId.equals("-1")) return;

        Call<List<ChatDTO>> call = apiService.getUserChats(Long.valueOf(userId)); // Теперь ChatDTO
        call.enqueue(new Callback<List<ChatDTO>>() {
            @Override
            public void onResponse(Call<List<ChatDTO>> call, Response<List<ChatDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ChatDTO> serverChats = response.body();
                    updateChatsListFromServer(serverChats);
                    System.out.println("✅ Загружено " + serverChats.size() + " чатов с сервера");
                } else {
                    System.out.println("❌ Ошибка загрузки чатов: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ChatDTO>> call, Throwable t) {
                System.out.println("❌ Ошибка сети: " + t.getMessage());
            }
        });
    }

    private void updateChatsListFromServer(List<ChatDTO> serverChats) {
        List<Chat> newChatList = new ArrayList<>();
        newChatList.add(new Chat("Избранное", "Загрузка...", "", true));

        for (ChatDTO serverChat : serverChats) {
            String otherUserName = getOtherUserNameFromServerChat(serverChat);
            String lastMessage = serverChat.getLastMessage() != null ? serverChat.getLastMessage() : "Нет сообщений";
            String time = serverChat.getLastMessageTime() != null ? formatServerTime(serverChat.getLastMessageTime()) : "";

            Chat androidChat = new Chat(otherUserName, lastMessage, time, false);
            newChatList.add(androidChat);
        }

        chatAdapter = new ChatAdapter(newChatList, this);
        setupClickListeners();
        chatsRecyclerView.setAdapter(chatAdapter);
    }

    private String getOtherUserNameFromServerChat(ChatDTO serverChat) {
        String currentUserId = sessionManager.getUserIdString();
        if (currentUserId == null) return "Unknown";

        Long currentId = Long.valueOf(currentUserId);

        if (serverChat.getUser1Id().equals(currentId)) {
            return serverChat.getUser2Name();
        } else {
            return serverChat.getUser1Name();
        }
    }

    private String formatServerTime(String serverTime) {
        if (serverTime == null || serverTime.isEmpty()) return "";
        try {
            return serverTime.substring(11, 16); // "2024-01-25T15:30:00" -> "15:30"
        } catch (Exception e) {
            return "";
        }
    }

    private void loadLastMessageForFavorites() {
        String userId = sessionManager.getUserIdString();
        if (userId == null || userId.equals("-1")) {
            updateFavoritesLastMessage("Войдите в систему", "");
            return;
        }

        String chatId = "favorites_" + userId;
        Call<Map<String, String>> call = apiService.getLastMessage(chatId);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, String> apiResponse = response.body();
                    if ("success".equals(apiResponse.get("status"))) {
                        String text = apiResponse.get("message");
                        String timestamp = apiResponse.get("timestamp");
                        updateFavoritesLastMessage(text, timestamp);
                    } else {
                        updateFavoritesLastMessage("Нет сообщений", "");
                    }
                } else {
                    updateFavoritesLastMessage("Ошибка загрузки", "");
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                updateFavoritesLastMessage("Ошибка сети", "");
            }
        });
    }
}