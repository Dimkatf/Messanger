package com.example.messager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
    //private static final String BASE_URL = "http://192.168.1.36:8080/";
    private static final String BASE_URL = "http://10.0.2.2:8080/";

    public ChatAdapter(List<Chat> chatList, Context context) {
        this.chatList = chatList;
        this.context = context;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        holder.chatName.setText(chat.getName());
        holder.time.setText(chat.getTime());

        if (chat.getName().equals("Избранное")) {
            loadLastMessageForFavorites(holder.lastMessage);
        } else {
            holder.lastMessage.setText(chat.getLastMessage());
        }

        holder.itemView.setOnClickListener(v -> {
            if (chat.getName().equals("Избранное")) {
                android.content.Intent intent = new android.content.Intent(context, FavoritesScreen.class);
                context.startActivity(intent);
            } else {
                android.widget.Toast.makeText(context, "Чат: " + chat.getName(), android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadLastMessageForFavorites(TextView lastMessageView) {
        Call<ApiResponse> call = apiService.getLastMessage("favorites");
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    System.out.println("📱 Adapter last message - Status: " + apiResponse.getStatus() + ", Message: " + apiResponse.getMessage());

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
                System.out.println("📱 Adapter last message error: " + t.getMessage());
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
