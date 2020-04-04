package com.saveetha.aidyou;

import android.util.Log;

import java.util.Timer;

public class AlteredTimer extends Timer {
    static boolean active = false;

    @Override
    public void cancel() {
        super.cancel();
        Log.i("AlteredTimer","TimerCanceled");
        active=false;
    }
}
