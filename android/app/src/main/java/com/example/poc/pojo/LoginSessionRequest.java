package com.example.poc.pojo;

import com.google.gson.annotations.SerializedName;

public class LoginSessionRequest {
    @SerializedName("id")
    public String id;
    @SerializedName("sessionId")
    public String sessionId;
    public LoginSessionRequest(String id, String sessionId) {
        this.id = id;
        this.sessionId = sessionId;
    }
}
