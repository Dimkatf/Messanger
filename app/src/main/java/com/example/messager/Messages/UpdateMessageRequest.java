package com.example.messager.Messages;

public class UpdateMessageRequest {
    private Long id;
    private String newText;

    public UpdateMessageRequest(Long id, String newText) {
        this.id = id;
        this.newText = newText;
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public String getNewText() {return newText;}
    public void setNewText(String newText) {this.newText = newText;}
}
