package com.example.messager;

public class AddUserName {
    private String phone;
    private String newUserName;

    AddUserName(){}
    AddUserName(String phone, String userName){
        this.phone = phone;
        this.newUserName = userName;
    }

    public String getUserName() {return newUserName;}
    public void setUserName(String userName) {this.newUserName = userName;}
    public String getPhone() {return phone;}
    public void setPhone(String phone) {this.phone = phone;}
}
