package com.example.poc.pojo;

import com.google.gson.annotations.SerializedName;

public class LogoutRequest {
    @SerializedName("id")
    public String id;
    public LogoutRequest(String id) {
        this.id = id;
    }
}