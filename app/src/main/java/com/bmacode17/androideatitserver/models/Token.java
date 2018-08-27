package com.bmacode17.androideatitserver.models;

/**
 * Created by User on 16-Aug-18.
 */

public class Token {

    // Making them public will make the "isServerToken" boolean to appear in Firebase.
    public String token;
    public boolean isServerToken;

    public Token() {
    }

    public Token(String token, boolean isServerToken) {
        this.token = token;
        this.isServerToken = isServerToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isServerToken() {
        return isServerToken;
    }

    public void setServerToken(boolean serverToken) {
        isServerToken = serverToken;
    }
}
