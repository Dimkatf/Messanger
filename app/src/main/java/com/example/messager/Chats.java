package com.example.messager;


import android.content.Intent;
import android.content.IntentFilter;
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

public class Chats extends AppCompatActivity {
    private Button profileBtn;
    private RecyclerView chatsRecyclerView;
    public ChatAdapter chatAdapter;
    private ApiService apiService;
    private static final String BASE_URL = "http://192.168.1.36:8080/";
    //private static final String BASE_URL = "http://10.0.2.2:8080/";

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
    public void updateFavoritesLastMessage(String newMessage) {
        if (chatAdapter != null) {
            chatAdapter.updateLastMessageForFavorites(newMessage);
        }
    }

    private void loadLastMessageForFavorites() {
        Call<ApiResponse> call = apiService.getLastMessage("favorites");
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if ("success".equals(apiResponse.getStatus())) {
                        String text = apiResponse.getMessage();
                        if (!text.equals("Нет сообщений")) {
                            updateFavoritesLastMessage(text);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
            }
        });
    }
}