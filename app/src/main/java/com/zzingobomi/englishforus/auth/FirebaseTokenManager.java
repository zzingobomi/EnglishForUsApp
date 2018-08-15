package com.zzingobomi.englishforus.auth;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.zzingobomi.englishforus.MainActivity;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogRecord;


public class FirebaseTokenManager {
    private static FirebaseTokenManager sInstance = new FirebaseTokenManager();

    private String token;
    private long expirationTime = 0;

    private FirebaseTokenManager() {}

    public static FirebaseTokenManager getInstance() {
        return sInstance;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    /*
    public String getFirebaseToken(FirebaseUser user) {
        if(user == null) {
            return null;
        }

        if(!user.isEmailVerified()) {
            return null;
        }

        // 최초 인증 토큰 받아오기
        if(expirationTime == 0) {


        }

        // 시간 만료로 다시 받아오기
        if(System.currentTimeMillis() > expirationTime) {

        }

        return token;
    }

    public boolean getRegreshFirebaseToken(FirebaseUser user) {
        user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if(task.isSuccessful()) {
                    String idToken = task.getResult().getToken();
                } else {

                }
            }
        });

        return false;
    }

    // 조금의 오차로 시간이 틀어졌으면 아예 다시 갱신해서 받아오기
    */


}
