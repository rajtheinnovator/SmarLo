package com.enpassio.smarlo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by ABHISHEK RAJ on 11/7/2017.
 */

public class BootCompleted extends BroadcastReceiver {
    public void onReceive(Context context, Intent arg1) {
        Intent intent = new Intent(context, LocationService.class);
        context.startService(intent);
    }
}
