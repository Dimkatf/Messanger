package com.example.messager;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<Chat> chatList;
    public ChatAdapter(List<Chat> chatList){
        this.chatList = chatList;
    }
    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position){
        Chat chat = chatList.get(position);
        holder.chatName.setText(chat.getName());
        holder.lastMessage.setText(chat.getLastMessage());
        holder.time.setText(chat.getTime());

        holder.itemView.setOnClickListener(v -> {
            if(chat.getName().equals("Избранное")){
                Intent intent = new Intent(v.getContext(), FavoritesScreen.class);
                v.getContext().startActivity(intent);
            }
            else android.widget.Toast.makeText(v.getContext(), "Чат: " + chat.getName(), android.widget.Toast.LENGTH_SHORT).show();
        });

    }
    @Override
    public int getItemCount(){
        return chatList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder{
        TextView chatName, lastMessage, time;
        public ChatViewHolder(@NonNull View itemView){
            super(itemView);
            chatName = itemView.findViewById(R.id.chatName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            time = itemView.findViewById(R.id.time);
        }
    }
}
