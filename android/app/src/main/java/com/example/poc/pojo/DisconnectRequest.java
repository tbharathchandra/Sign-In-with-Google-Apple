package com.example.poc.pojo;

import com.google.gson.annotations.SerializedName;

public class DisconnectRequest {
    @SerializedName("id")
    public String id;
    public DisconnectRequest(String id) {
        this.id = id;
    }
}
