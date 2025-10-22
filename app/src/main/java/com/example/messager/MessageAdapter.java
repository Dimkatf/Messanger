package com.example.messager;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
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

        if (message.isEdited()) {
            holder.editedText.setVisibility(View.VISIBLE);
        } else {
            holder.editedText.setVisibility(View.GONE);
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

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView editedText;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            editedText = itemView.findViewById(R.id.editedText);
        }
    }
}
