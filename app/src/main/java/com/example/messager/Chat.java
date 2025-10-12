package com.example.messager;

public class Chat {
    private String name;
    private String lastMessage;
    private String time;
    private boolean isFavorite;

    public Chat(String name, String lastMessage, String time, boolean isFavorite){
        this.name = name;
        this.lastMessage = lastMessage;
        this.time = time;
        this.isFavorite = isFavorite;
    }
    public String getName() {return name;}
    public String getLastMessage() {return lastMessage;}
    public String getTime() {return time;}
    public boolean isFavorite() { return isFavorite; }
}
