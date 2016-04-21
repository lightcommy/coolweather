package com.commy.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.commy.service.MyService;

/**
 * Created by Commy on 2016/4/21.
 */
public class AutoUpdateReceiver extends BroadcastReceiver {
    public void onReceive(Context context,Intent intent){
        Intent i = new Intent(context, MyService.class);
        context.startService(i);
    }
}
