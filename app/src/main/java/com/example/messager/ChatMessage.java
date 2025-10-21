package com.example.messager;

public class ChatMessage {
    private Long id;
    private String chatId;
    private String sender;
    private String text;
    private boolean edited;

    public ChatMessage() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public boolean isEdited() { return edited; }
    public void setEdited(boolean edited) { this.edited = edited; }
}