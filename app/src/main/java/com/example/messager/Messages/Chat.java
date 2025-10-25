package com.example.messager.Messages;

public class Chat {
    private String name;
    private String lastMessage;
    private String time;
    private boolean isFavorite;

    public Chat(String name, String lastMessage, String time, boolean isFavorite) {
        this.name = name;
        this.lastMessage = lastMessage;
        this.time = time;
        this.isFavorite = isFavorite;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}