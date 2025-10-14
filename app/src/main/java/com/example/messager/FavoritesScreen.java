package com.example.messager;


import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
    //private static final String BASE_URL = "http://192.168.1.36:8080/";
    private static final String BASE_URL = "http://10.0.2.2:8080/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites_activity);

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
        messageAdapter = new MessageAdapter(messageList);
        messagesRecyclerView.setAdapter(messageAdapter);

        exitFavoritesBtn.setOnClickListener(v -> finish());

        sendMessageBtn.setOnClickListener(v -> {
            String text = editMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessageToServer(text);
                editMessage.setText("");
            }
        });

        loadAllMessages();
    }

    private void sendMessageToServer(String text) {
        Map<String, String> request = new HashMap<>();
        request.put("chatId", "favorites");
        request.put("text", text);

        System.out.println("📱 Sending message to: " + BASE_URL + "api/send-message");
        System.out.println("📱 Message text: " + text);

        Call<ApiResponse> call = apiService.sendMessage(request); // ★★★★ ApiResponse вместо String
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                System.out.println("📱 Send response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    System.out.println("📱 Server response - Status: " + apiResponse.getStatus() + ", Message: " + apiResponse.getMessage());

                    if ("success".equals(apiResponse.getStatus())) {
                        System.out.println("📱 Message sent successfully!");
                        loadAllMessages();
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
                System.out.println("📱 Send network error: " + t.getMessage());
                Toast.makeText(FavoritesScreen.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadAllMessages() {
        Call<List<ChatMessage>> call = apiService.getMessages("favorites");
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
}
