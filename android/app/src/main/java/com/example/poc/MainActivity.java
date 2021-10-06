package com.example.poc;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.poc.pojo.LoginRequest;
import com.example.poc.pojo.LoginResponse;
import com.example.poc.pojo.LoginSessionRequest;
import com.example.poc.pojo.LoginSessionResponse;
import com.example.poc.rest.APIClient;
import com.example.poc.rest.APIInterface;
import com.example.poc.utils.SharedPrefManger;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private SignInButton googleSignIn;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "MainActivity";
    APIInterface apiInterface;
    private Button disconnect;
    private SharedPrefManger sharedPrefManger;
    private CircularProgressIndicator progressIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        googleSignIn = findViewById(R.id.sign_in_button);
        disconnect = findViewById(R.id.disconnect_btn);
        progressIndicator = findViewById(R.id.progress_circular);
        apiInterface = APIClient.getClient().create(APIInterface.class);

        createGoogleSigninClient();

        googleSignIn.setOnClickListener(view -> {
            progressIndicator.setVisibility(View.VISIBLE);
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        disconnect.setOnClickListener(v -> {
            progressIndicator.setVisibility(View.VISIBLE);
            googleSignInClient.revokeAccess().addOnCompleteListener(task -> {
                progressIndicator.setVisibility(View.INVISIBLE);
                Toast.makeText(MainActivity.this, "Success disconnect", Toast.LENGTH_LONG).show();
            }).addOnFailureListener(e->{
                progressIndicator.setVisibility(View.INVISIBLE);
            });
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        sharedPrefManger = SharedPrefManger.getSharedPrefManger(getApplicationContext());
        if(!sharedPrefManger.getSessionid().equals("")){
            Toast.makeText(MainActivity.this, "Checking Session Token.. Please wait", Toast.LENGTH_LONG).show();
            checkLoginStatus(sharedPrefManger.getSessionid(), sharedPrefManger.getUserid());
        }else{
            Toast.makeText(MainActivity.this, "Please Sign In with Google", Toast.LENGTH_LONG).show();
        }
    }

    private void checkLoginStatus(String sessionid, String userid) {
        LoginSessionRequest loginSessionRequest = new
                LoginSessionRequest(userid, sessionid);
        Call<LoginSessionResponse> loginSessionResponseCall = apiInterface.loginWithSessionToken(loginSessionRequest);
        loginSessionResponseCall.enqueue(new Callback<LoginSessionResponse>() {
            @Override
            public void onResponse(Call<LoginSessionResponse> call, Response<LoginSessionResponse> response) {
                progressIndicator.setVisibility(View.INVISIBLE);
                if(response.isSuccessful()){
                    LoginResponse loginResponse = new LoginResponse();
                    loginResponse.name = sharedPrefManger.getName();
                    loginResponse.email = sharedPrefManger.getEmailId();
                    loginResponse.sessionId = sharedPrefManger.getSessionid();
                    loginResponse.id = sharedPrefManger.getUserid();

                    Toast.makeText(MainActivity.this, "Login Session token success", Toast.LENGTH_LONG).show();
                    launchLandingActivity(loginResponse);
                }else {
                    Toast.makeText(MainActivity.this, "Login with session token failed, try sign in with google", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginSessionResponse> call, Throwable t) {
                progressIndicator.setVisibility(View.INVISIBLE);
                t.printStackTrace();
                Toast.makeText(MainActivity.this, "Login with session token failed", Toast.LENGTH_LONG).show();
            }
        });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);

            Log.d(TAG, "user id-"+account.getId());
            Log.d(TAG, "user name-"+account.getDisplayName());
            Log.d(TAG, "email-"+account.getEmail());
            Log.d(TAG, "id token-"+account.getIdToken());
            Log.d(TAG, "auth code-"+account.getServerAuthCode());


            loginUserWithGoogle(account.getIdToken(), account.getServerAuthCode(), account.getId());

        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void loginUserWithGoogle(String idToken, String authCode, String providerId) {
        LoginRequest loginRequest = new LoginRequest(idToken, authCode, providerId);
        Call<LoginResponse> loginCall = apiInterface.loginUserWithGoogle(loginRequest);
        loginCall.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                progressIndicator.setVisibility(View.INVISIBLE);
                if(response.isSuccessful()){
                    Toast.makeText(MainActivity.this, "Login success - Backend", Toast.LENGTH_LONG).show();
                    storeSessionDetails(response.body());
                    launchLandingActivity(response.body());
                }else {
                    signOutFromGoogle();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                progressIndicator.setVisibility(View.INVISIBLE);
                signOutFromGoogle();
            }
        });
    }

    private void storeSessionDetails(LoginResponse loginResponse) {
        sharedPrefManger.setSessionId(loginResponse.sessionId);
        sharedPrefManger.setUserId(loginResponse.id);
        sharedPrefManger.setName(loginResponse.name);
        sharedPrefManger.setEmailId(loginResponse.email);
    }

    private void launchLandingActivity(LoginResponse loginResponse) {
        Intent intent = new Intent(MainActivity.this, LandingActivity.class);
        intent.putExtra("name", loginResponse.name);
        intent.putExtra("email", loginResponse.email);
        intent.putExtra("id", loginResponse.id);
        intent.putExtra("sessionId", loginResponse.sessionId);
        startActivity(intent);
    }

    private void signOutFromGoogle() {
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Toast.makeText(MainActivity.this, "Login failed - Backend", Toast.LENGTH_LONG).show();
        });
    }
}