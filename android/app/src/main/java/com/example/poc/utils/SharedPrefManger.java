package com.example.poc.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManger {
    private static SharedPrefManger sharedPrefManger = null;
    public static final String MyPREFERENCES = "MyPrefs";
    public static final String SESSION_ID = "SessionID";
    public static final String USER_ID = "UserID";
    public static final String NAME = "name";
    public static final String EMAIL = "email";

    SharedPreferences sharedpreferences = null;
    SharedPreferences.Editor editor = null;

    public SharedPrefManger(Context context) {
        if(sharedpreferences==null) sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        if(editor==null) editor = sharedpreferences.edit();
    }

    public static SharedPrefManger getSharedPrefManger(Context context) {
        if(sharedPrefManger==null) sharedPrefManger = new SharedPrefManger(context);
        return sharedPrefManger;
    }

    public void setSessionId(String sessionId){
        editor.putString(SESSION_ID, sessionId);
        editor.apply();
    }

    public String getSessionid() {
        return sharedpreferences.getString(SESSION_ID, "");
    }

    public void setUserId(String userId){
        editor.putString(USER_ID, userId);
        editor.apply();
    }

    public String getUserid() {
        return sharedpreferences.getString(USER_ID, "");
    }

    public void setName(String name){
        editor.putString(NAME, name);
        editor.apply();
    }

    public String getName() {
        return sharedpreferences.getString(NAME, "");
    }

    public void setEmailId(String email){
        editor.putString(EMAIL, email);
        editor.apply();
    }

    public String getEmailId() {
        return sharedpreferences.getString(EMAIL, "");
    }

    public void clearSharedPrefs() {
        editor.clear();
        editor.commit();
    }
}
