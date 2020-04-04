package com.saveetha.aidyou;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button editContactDetails,serviceButton,cancelTrigger,resetButton,sosbutton;
    SeekBar delaySeekbar;
    SharedPreferences sharedPreferences;
    TextView tv;
    LocationManager locationManager;
    String alertMessage;
    Location location;
    SmsManager smsManager;
    private Handler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("PreferenceAidYou",MODE_PRIVATE);
        if (sharedPreferences.getBoolean("isFirstTime",true)) {
            getContactDetails();
        }
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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

        sosbutton = findViewById(R.id.sosbutton);
        sosbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.e("Main Activity","SOS Button Triggered");
                Toast.makeText(MainActivity.this,"SOS Sent",Toast.LENGTH_LONG).show();
                alertMessage = "Help! I am in danger!";
                location=getLocation();
                if(location==null){
                    alertMessage+=" My location is disabled! Contact me ASAP";
                }else{
                    // https://www.google.com/maps/search/?api=1&query=47.5951518,-122.3316393
                    alertMessage+=" My location is "+location.getLatitude()+","+location.getLongitude()+", Click on the link- https://www.google.com/maps/search/?api=1&query="+location.getLatitude()+","+
                            location.getLongitude();
                }
                smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(sharedPreferences.getString("EmergencyNum1","9999999990"),null,alertMessage,null,null);
                smsManager.sendTextMessage(sharedPreferences.getString("EmergencyNum2","9999999990"),null,alertMessage,null,null);
                smsManager.sendTextMessage(sharedPreferences.getString("EmergencyNum3","9999999990"),null,alertMessage,null,null);
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
    Location getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return null;
        }
        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

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
