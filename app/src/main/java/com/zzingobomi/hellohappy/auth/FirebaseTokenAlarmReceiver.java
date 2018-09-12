package com.zzingobomi.hellohappy.auth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class FirebaseTokenAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "알람이 울림", Toast.LENGTH_SHORT).show();

        FirebaseTokenManager.getInstance().refreshToken(context, null);
    }
}
