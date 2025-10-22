package com.example.messager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    public List<Chat> chatList;
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        holder.chatName.setText(chat.getName());

        if (chat.getTime() != null && !chat.getTime().isEmpty()) {
            holder.time.setText(chat.getTime());
        } else {
            holder.time.setText("");
        }

        if (chat.getLastMessage() != null && !chat.getLastMessage().isEmpty()) {
            holder.lastMessage.setText(chat.getLastMessage());
        } else {
            holder.lastMessage.setText("Нет сообщений");
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

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public void updateFavoritesLastMessage(String newMessage, String timestamp) {
        for (int i = 0; i < chatList.size(); i++) {
            Chat chat = chatList.get(i);
            if (chat.getName().equals("Избранное")) {

                chat.setLastMessage(newMessage);

                if (timestamp != null && !timestamp.isEmpty() && !"Нет сообщений".equals(newMessage)) {
                    String formattedTime = formatTime(timestamp);
                    chat.setTime(formattedTime);
                } else chat.setTime("");

                notifyItemChanged(i);
                break;
            }
        }
    }

    private String formatTime(String timestamp) {
        try {
            SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            Date date = serverFormat.parse(timestamp);
            return displayFormat.format(date);
        } catch (Exception e) {
            System.out.println("❌ Ошибка форматирования времени: " + e.getMessage());
            return "";
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