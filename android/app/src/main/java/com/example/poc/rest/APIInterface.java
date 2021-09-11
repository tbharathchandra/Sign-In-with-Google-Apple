package com.example.poc.rest;

import com.example.poc.pojo.DisconnectRequest;
import com.example.poc.pojo.DisconnectResponse;
import com.example.poc.pojo.LoginRequest;
import com.example.poc.pojo.LoginResponse;
import com.example.poc.pojo.LoginSessionRequest;
import com.example.poc.pojo.LoginSessionResponse;
import com.example.poc.pojo.LogoutRequest;
import com.example.poc.pojo.LogoutResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface APIInterface {
    @POST("/loginWithGoogle")
    Call<LoginResponse> loginUserWithGoogle(@Body LoginRequest loginRequest);

    @POST("/loginWithSessionToken")
    Call<LoginSessionResponse> loginWithSessionToken(@Body LoginSessionRequest loginSessionRequest);

    @POST("/logout")
    Call<LogoutResponse> logout(@Body LogoutRequest logoutRequest);

    @POST("/disconnect")
    Call<DisconnectResponse> disconnectUser(@Body DisconnectRequest disconnectRequest);

}
