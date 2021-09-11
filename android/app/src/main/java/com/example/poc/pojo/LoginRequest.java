package com.example.poc.pojo;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    @SerializedName("idToken")
    public String idToken;
    @SerializedName("authCode")
    public String authCode;
    @SerializedName("providerId")
    public String providerId;
    public LoginRequest(String idToken, String authCode, String providerId) {
        this.idToken = idToken;
        this.authCode = authCode;
        this.providerId = providerId;
    }
}
