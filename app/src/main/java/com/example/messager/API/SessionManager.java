// SessionManager.java
package com.example.messager.API;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_USERNAME = "user_username";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createSession(Long userId, String userName, String userPhone) {
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USER_PHONE, userPhone);
        editor.apply();
    }

    public void saveUsername(String username) {
        editor.putString(KEY_USER_USERNAME, username);
        editor.apply();
    }

    public String getUsername() {
        return pref.getString(KEY_USER_USERNAME, "");
    }

    public Long getUserId() {
        return pref.getLong(KEY_USER_ID, -1);
    }

    public String getUserIdString() {
        return String.valueOf(pref.getLong(KEY_USER_ID, -1));
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "");
    }

    public String getUserPhone() {
        return pref.getString(KEY_USER_PHONE, "");
    }

    public boolean isLoggedIn() {
        return getUserId() != -1 && !getUserPhone().isEmpty();
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}