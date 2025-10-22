package com.example.messager;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.widget.Toast;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<ChatMessage> messageList;
    private Context context;
    private ApiService apiService;
    private String currentUserId;
    private FavoritesScreen activity;

    public MessageAdapter(List<ChatMessage> messageList, Context context, ApiService apiService, String currentUserId) {
        this.messageList = messageList;
        this.context = context;
        this.apiService = apiService;
        this.currentUserId = currentUserId;

        if (context instanceof FavoritesScreen) {
            this.activity = (FavoritesScreen) context;
        }
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

        if (message.getTimestamp() != null && !message.getTimestamp().isEmpty()) {
            holder.timeText.setText(formatTime(message.getTimestamp()));
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            holder.timeText.setText(sdf.format(new Date()));
        }

        if (message.isEdited()) {
            holder.editedText.setVisibility(View.VISIBLE);
            holder.bubbleLayout.setPadding(
                    holder.bubbleLayout.getPaddingLeft(),
                    holder.bubbleLayout.getPaddingTop(),
                    holder.bubbleLayout.getPaddingRight(),
                    12
            );
        } else {
            holder.editedText.setVisibility(View.GONE);
            holder.bubbleLayout.setPadding(
                    holder.bubbleLayout.getPaddingLeft(),
                    holder.bubbleLayout.getPaddingTop(),
                    holder.bubbleLayout.getPaddingRight(),
                    8
            );
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (activity != null) {
                showContextMenu(message, position);
            }
            return true;
        });
    }

    private void showContextMenu(ChatMessage message, int position) {
        new AlertDialog.Builder(context)
                .setItems(new String[]{"Изменить", "Удалить", "Закрепить", "Скопировать"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showEditDialog(message, position);
                            break;
                        case 1:
                            showDeleteConfirmation(message, position);
                            break;
                        case 2:
                            pinMessage(message);
                            break;
                        case 3:
                            copyMessage(message);
                            break;
                    }
                })
                .show();
    }

    private void showEditDialog(ChatMessage message, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Изменить сообщение");

        final EditText input = new EditText(context);
        input.setText(message.getText());
        builder.setView(input);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String newText = input.getText().toString().trim();
            if (!newText.isEmpty() && !newText.equals(message.getText())) {
                updateMessage(message.getId(), newText, position);
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void showDeleteConfirmation(ChatMessage message, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Удалить сообщение?")
                .setMessage("Вы уверены, что хотите удалить это сообщение?")
                .setPositiveButton("Удалить", (dialog1, which1) -> {
                    if (activity != null) {
                        activity.deleteMessage(message.getId(), position);
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void updateMessage(Long messageId, String newText, int position) {
        UpdateMessageRequest request = new UpdateMessageRequest(messageId, newText);

        apiService.updateMessage(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    ChatMessage message = messageList.get(position);
                    message.setText(newText);
                    message.setEdited(true);
                    notifyItemChanged(position);
                    Toast.makeText(context, "Сообщение обновлено", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Ошибка обновления", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(context, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pinMessage(ChatMessage message) {
        Toast.makeText(context, "Сообщение закреплено", Toast.LENGTH_SHORT).show();
    }

    private void copyMessage(ChatMessage message) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Сообщение", message.getText());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Сообщение скопировано", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private String formatTime(String timestamp) {
        try {
            SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            Date date = serverFormat.parse(timestamp);
            return displayFormat.format(date);
        } catch (Exception e) {
            return timestamp;
        }
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView editedText;
        TextView timeText;
        LinearLayout bubbleLayout;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            editedText = itemView.findViewById(R.id.editedText);
            timeText = itemView.findViewById(R.id.timeText);
            bubbleLayout = itemView.findViewById(R.id.bubbleLayout);
        }
    }
}
