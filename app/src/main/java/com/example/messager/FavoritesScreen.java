package com.example.messager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class FavoritesScreen extends AppCompatActivity {
    private Button exitFavoritesBtn;
    private Button sendMessageBtn;
    private TextView messageText;
    private EditText editMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.favorites_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.favorite), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        exitFavoritesBtn = findViewById(R.id.backFavorites);
        exitFavoritesBtn.setOnClickListener(v -> {
            finish();
        });
        sendMessageBtn = findViewById(R.id.sendMessageFavorites);
        messageText = findViewById(R.id.messageFavorites);
        editMessage = findViewById(R.id.messageForFavoritesEdit);

        sendMessageBtn.setOnClickListener(v -> {
            String text = editMessage.getText().toString();
            if (!text.isEmpty()){
                messageText.setText(text);
                editMessage.setText("");
            }
        });
    }
}
