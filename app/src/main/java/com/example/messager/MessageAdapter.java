package com.example.messager;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<ChatMessage> messageList;
    private FavoritesScreen activity;
    private ApiService apiService;

    public MessageAdapter(List<ChatMessage> messageList, FavoritesScreen activity, ApiService apiService) {
        this.messageList = messageList;
        this.activity = activity;
        this.apiService = apiService;
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

        if(message.isChange())
            holder.editedText.setVisibility(View.VISIBLE);
        else holder.editedText.setVisibility(View.GONE);

        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(activity)
                    .setItems(new String[]{"Изменить", "Удалить"}, (dialog, which) -> {
                        switch (which){
                            case 0:
                                showEditDialog(message, position);
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

    private void showEditDialog(ChatMessage message, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Изменить сообщение");

        final EditText input = new EditText(activity);
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

    private void updateMessage(Long messageId, String newText, int position) {
        UpdateMessageRequest request = new UpdateMessageRequest(messageId, newText);

        apiService.updateMessage(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    ChatMessage message = messageList.get(position);
                    message.setText(newText);
                    message.setChange(true);
                    notifyItemChanged(position);
                    Toast.makeText(activity, "Сообщение обновлено", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, "Ошибка обновления", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(activity, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
