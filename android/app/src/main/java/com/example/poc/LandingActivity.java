package com.example.poc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.poc.pojo.DisconnectRequest;
import com.example.poc.pojo.DisconnectResponse;
import com.example.poc.pojo.LoginResponse;
import com.example.poc.pojo.LogoutRequest;
import com.example.poc.pojo.LogoutResponse;
import com.example.poc.rest.APIClient;
import com.example.poc.rest.APIInterface;
import com.example.poc.utils.SharedPrefManger;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LandingActivity extends AppCompatActivity {

    private Button googleSignOut, googleDisconnect;
    private LoginResponse loginResponse;
    private GoogleSignInClient googleSignInClient;
    private TextView name, email, sessionToken, id;
    private SharedPrefManger sharedPrefManger;
    APIInterface apiInterface;
    private CircularProgressIndicator progressIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        loginResponse = new LoginResponse();
        loginResponse.name = getIntent().getStringExtra("name");
        loginResponse.email = getIntent().getStringExtra("email");
        loginResponse.id = getIntent().getStringExtra("id");
        loginResponse.sessionId = getIntent().getStringExtra("sessionId");

        sharedPrefManger = SharedPrefManger.getSharedPrefManger(getApplicationContext());
        apiInterface = APIClient.getClient().create(APIInterface.class);

        setupViews();

        createGoogleSigninClient();
        attachListeners();

        initView();
    }

    private void setupViews() {
        googleDisconnect = findViewById(R.id.disconnect_btn);
        googleSignOut = findViewById(R.id.logout_btn);
        name = findViewById(R.id.name_tv);
        email = findViewById(R.id.email_tv);
        id = findViewById(R.id.id_tv);
        sessionToken = findViewById(R.id.sessionToken_tv);
        progressIndicator = findViewById(R.id.progress_circular);
    }

    private void initView() {
        name.setText(loginResponse.name);
        email.setText(loginResponse.email);
        id.setText(loginResponse.id);
        sessionToken.setText(loginResponse.sessionId);
    }

    private void createGoogleSigninClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .requestId()
                .requestIdToken(getString(R.string.server_client_id))
                .requestServerAuthCode(getString(R.string.server_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void attachListeners() {
        googleSignOut.setOnClickListener(view -> signOutFromGoogle());
        googleDisconnect.setOnClickListener(view -> disconnectFromGoogle());
    }

    private void disconnectFromGoogle() {
        progressIndicator.setVisibility(View.VISIBLE);
        googleSignInClient.revokeAccess().addOnCompleteListener(task -> {
            disconnectUser();
        }).addOnFailureListener(e -> {
            Toast.makeText(LandingActivity.this, "Failed Google disconnect - local", Toast.LENGTH_LONG).show();
            progressIndicator.setVisibility(View.INVISIBLE);
        });
    }

    private void disconnectUser() {
        DisconnectRequest disconnectRequest = new DisconnectRequest(sharedPrefManger.getUserid());
        Call<DisconnectResponse> disconnectResponseCall = apiInterface.disconnectUser(disconnectRequest);
        disconnectResponseCall.enqueue(new Callback<DisconnectResponse>() {
            @Override
            public void onResponse(Call<DisconnectResponse> call, Response<DisconnectResponse> response) {
                progressIndicator.setVisibility(View.INVISIBLE);
                if(response.isSuccessful()){
                    Toast.makeText(LandingActivity.this, "Success disconnect", Toast.LENGTH_LONG).show();
                    sharedPrefManger.clearSharedPrefs();
                    launchMainActivity();
                }else {
                    Toast.makeText(LandingActivity.this, "Failed disconnect - backend", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<DisconnectResponse> call, Throwable t) {
                progressIndicator.setVisibility(View.INVISIBLE);
                Toast.makeText(LandingActivity.this, "Failed disconnect - backend", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void signOutFromGoogle() {
        progressIndicator.setVisibility(View.VISIBLE);
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            logoutUser();
        }).addOnFailureListener(e -> {
            Toast.makeText(LandingActivity.this, "Failed Google Logout - local", Toast.LENGTH_LONG).show();
            progressIndicator.setVisibility(View.INVISIBLE);
        });
    }

    private void logoutUser() {
        LogoutRequest logoutRequest = new LogoutRequest(sharedPrefManger.getUserid());
        Call<LogoutResponse> logoutResponseCall = apiInterface.logout(logoutRequest);
        logoutResponseCall.enqueue(new Callback<LogoutResponse>() {
            @Override
            public void onResponse(Call<LogoutResponse> call, Response<LogoutResponse> response) {
                progressIndicator.setVisibility(View.INVISIBLE);
                if(response.isSuccessful()){
                    Toast.makeText(LandingActivity.this, "Success Logout", Toast.LENGTH_LONG).show();
                    sharedPrefManger.clearSharedPrefs();
                    launchMainActivity();
                }else {
                    Toast.makeText(LandingActivity.this, "Failed Logout - backend", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LogoutResponse> call, Throwable t) {
                progressIndicator.setVisibility(View.INVISIBLE);
                Toast.makeText(LandingActivity.this, "Failed Logout - backend", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void launchMainActivity() {
        Intent intent = new Intent(LandingActivity.this, MainActivity.class);
        startActivity(intent);
    }


}