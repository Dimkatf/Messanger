package com.example.messager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<Chat> chatList;
    private Context context;
    private ApiService apiService;
    private SessionManager sessionManager;
    private static final String BASE_URL = "http://192.168.1.36:8080/";

    public ChatAdapter(List<Chat> chatList, Context context) {
        this.chatList = chatList;
        this.context = context;
        this.sessionManager = new SessionManager(context);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ИСПРАВЛЕНИЕ: нужно вернуть реальный ViewHolder, а не null
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false); // Убедитесь, что item_chat существует
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        holder.chatName.setText(chat.getName());
        holder.time.setText(chat.getTime());

        if (chat.getName().equals("Избранное")) {
            String userId = sessionManager.getUserIdString();
            if (userId != null && !userId.equals("-1")) {
                loadLastMessageForFavorites(holder.lastMessage, userId);
            } else {
                holder.lastMessage.setText("Нет сообщений");
            }
        } else {
            holder.lastMessage.setText(chat.getLastMessage());
        }

        holder.itemView.setOnClickListener(v -> {
            if (chat.getName().equals("Избранное")) {
                if (sessionManager.isLoggedIn()) {
                    android.content.Intent intent = new android.content.Intent(context, FavoritesScreen.class);
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Пожалуйста, войдите в систему", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Чат: " + chat.getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadLastMessageForFavorites(TextView lastMessageView, String userId) {
        Call<ApiResponse> call = apiService.getLastMessage("favorites_" + userId);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if ("success".equals(apiResponse.getStatus())) {
                        String text = apiResponse.getMessage();
                        lastMessageView.setText(text);
                    } else {
                        lastMessageView.setText("Нет сообщений");
                    }
                } else {
                    lastMessageView.setText("Нет сообщений");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                lastMessageView.setText("Нет сообщений");
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public void updateLastMessageForFavorites(String newMessage) {
        for (int i = 0; i < chatList.size(); i++) {
            Chat chat = chatList.get(i);
            if (chat.getName().equals("Избранное")) {
                chat.setLastMessage(newMessage);
                notifyItemChanged(i);
                break;
            }
        }
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView chatName, lastMessage, time;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            chatName = itemView.findViewById(R.id.chatName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            time = itemView.findViewById(R.id.time);
        }
    }
}