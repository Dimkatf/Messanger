package com.example.messager;

public class User {
    private Long id;
    private String name;
    private String phone;
    private String userName;

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}