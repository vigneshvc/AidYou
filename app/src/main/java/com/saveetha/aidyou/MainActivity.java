package com.saveetha.aidyou;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button editContactDetails,serviceButton,cancelTrigger,resetButton;
    SeekBar delaySeekbar;
    SharedPreferences sharedPreferences;
    TextView tv;
    private Handler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("PreferenceAidYou",MODE_PRIVATE);
        if (sharedPreferences.getBoolean("isFirstTime",true)) {
            getContactDetails();
        }
        setContentView(R.layout.activity_main);

        mHandler = new Handler();
    }

    @Override
    protected void onStart() {


        super.onStart();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
                        0);
            }
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
                        0);
            }
        }
        tv = findViewById(R.id.displayDelay);
        cancelTrigger = findViewById(R.id.cancelTrigger);
        cancelTrigger.setOnClickListener(this);

        resetButton = findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(MainActivity.this,FallDetection.class));
                stopService(new Intent(MainActivity.this,FallDetection.class));
                startActivity(new Intent(MainActivity.this,MainActivity.class));
                finish();
            }
        });

        editContactDetails=findViewById(R.id.editContactDetails);
        editContactDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),ContactFetchActivity.class));
            }
        });
        delaySeekbar = findViewById(R.id.delaySeekbar);
        delaySeekbar.setProgress(sharedPreferences.getInt("delayTime",10));
        tv.setText("Delay Period: "+delaySeekbar.getProgress()+"S");


        delaySeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = seekBar.getProgress();
                tv.setText("Delay Period: "+value+"S");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sharedPreferences.edit().putInt("delayTime",seekBar.getProgress()).apply();
            }
        });
        serviceButton = findViewById(R.id.serviceButton);
        if(getSharedPreferences("PreferenceAidYou",0).getBoolean("ServiceOn",false)){
            serviceButton.setText("Stop Service");
        }
        else{
            serviceButton.setText("Start Service");
        }
        serviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(getApplicationContext(),FallDetection.class);
                if(getSharedPreferences("PreferenceAidYou",0).getBoolean("ServiceOn",false)){
                    // turn off the service
                    Log.i("caller","def");
                    stopService(serviceIntent);
                    serviceButton.setText("Start Service");
                }else{
                    // turn on the service
                    Log.i("caller","abc");
                    startService(serviceIntent);
                    serviceButton.setText("Stop Service");
                }
            }
        });
        startProgress();
    }
    private void startProgress() {
//      New thread to perform background operation
        new Thread(new Runnable() {
            @Override
            public void run() {while(true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelTrigger.setEnabled(AlteredTimer.active);
                    }
                });
            }
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cancelTrigger.setEnabled(AlteredTimer.active);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void getContactDetails() {
        Intent intent = new Intent(this,ContactFetchActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == cancelTrigger.getId()){
            FallDetection.timer.cancel();
            cancelTrigger.setEnabled(AlteredTimer.active);
        }
    }
}
