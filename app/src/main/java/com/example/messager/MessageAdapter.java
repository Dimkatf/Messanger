package com.example.messager;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.widget.Button;
import android.widget.LinearLayout;

import retrofit2.Retrofit;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<ChatMessage> messageList;
    private FavoritesScreen activity;

    public MessageAdapter(List<ChatMessage> messageList, FavoritesScreen activity) {
        this.messageList = messageList;
        this.activity = activity;

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        holder.messageText.setText(message.getText());

        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(activity)
                    .setItems(new String[]{"Изменить", "Удалить"}, (dialog, which) -> {
                        switch (which){
                            case 0:
                                break;
                            case 1:
                            new AlertDialog.Builder(activity)
                                    .setTitle("Удалить сообщение?")
                                    .setMessage("Вы уверены, что хотите удалить это сообщение?")
                                    .setPositiveButton("Удалить", (dialog1, which1) -> {
                                        activity.deleteMessage(message.getId(), position);
                                    })
                                    .setNegativeButton("Отмена", null)
                                    .show();
                            break;
                        }
                    })
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
        }
    }
}
