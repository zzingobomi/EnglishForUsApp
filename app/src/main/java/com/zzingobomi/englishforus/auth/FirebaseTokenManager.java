package com.zzingobomi.englishforus.auth;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogRecord;


public class FirebaseTokenManager {
    private static FirebaseTokenManager sInstance = new FirebaseTokenManager();

    private FirebaseUser mFirebaseUser;

    private String token;
    private long expirationTime = 0;

    private Context mContext;
    private AlarmManager mAlarmManager;
    private PendingIntent mSender;

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

    ///
    /// 토큰 갱신해서 받아오기
    ///
    public void refreshToken(Context context, FirebaseUser user) {
        if(context != null) {
            mContext = context;
        }

        if(user != null) {
            mFirebaseUser = user;
        }

        if(mFirebaseUser == null) {
            Log.d("FirebaseTokenMnager", "FirebaseUser Null!!");
            return;
        }

        mFirebaseUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if(task.isSuccessful()) {
                    token = task.getResult().getToken();
                    expirationTime = task.getResult().getExpirationTimestamp();

                    // 왜 2번 호출..?
                    //Log.d("TAG", "getIdToken success toekn manager : " + token + " / " + expirationTime);
                    Date date = new Date(expirationTime * 1000);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.KOREA);
                    Log.d("TAG", "만료시간 : " + expirationTime + " / " +  dateFormat.format(date));

                    setAlarm();
                } else {
                    token = null;
                    expirationTime = 0;
                    Log.d("TAG", "TgetIdToken fail " + task.getException());
                }
            }
        });
    }

    private void setAlarm() {
        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(mContext, FirebaseTokenAlarmReceiver.class);
        mSender = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        mAlarmManager.set(AlarmManager.RTC, (expirationTime * 1000) - 3000, mSender);
    }

    ///
    /// 토큰 갱신 알람 멈추기
    ///
    public void stopTokenRefresh() {
        mAlarmManager.cancel(mSender);
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
