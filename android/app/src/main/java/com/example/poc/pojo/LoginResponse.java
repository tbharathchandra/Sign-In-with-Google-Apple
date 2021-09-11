package com.example.poc.pojo;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LoginResponse implements Serializable {
    @SerializedName("name")
    public String name;
    @SerializedName("email")
    public String email;
    @SerializedName("sessionId")
    public String sessionId;
    @SerializedName("id")
    public String id;
}
