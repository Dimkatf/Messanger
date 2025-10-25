package com.example.messager.Messages;

public class ChatMessage {
    private Long id;
    private String chatId;
    private String sender;
    private String text;
    private  String timestamp;
    private boolean edited;

    public ChatMessage() {}
    public ChatMessage(Long id, String chatId, String sender, String text, String timestamp, boolean edited){
        this.id = id;
        this.chatId = chatId;
        this.sender = sender;
        this.text = text;
        this.timestamp = timestamp;
        this.edited = edited;
    }

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
    public String getTimestamp() {return timestamp;}
    public void setTimestamp(String timestamp) {this.timestamp = timestamp;}
}