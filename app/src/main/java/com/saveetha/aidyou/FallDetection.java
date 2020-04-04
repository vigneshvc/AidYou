package com.saveetha.aidyou;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import java.util.TimerTask;

public class FallDetection extends Service implements SensorEventListener {

    int delay;
    SensorManager sensorManager;
    SmsManager smsManager;
    Sensor sensorAccelerometer;
    String CHANNEL_ID = "ForegroundServiceChannel";
    SharedPreferences sharedPreferences;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    int SHAKE_THRESHOLD = 900;
    LocationManager locationManager;
    Location location;
    String alertMessage;
    public static AlteredTimer timer;
    long curTime;
    float x,y,z;
    public FallDetection() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("FallDetection", "Started");
        Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sharedPreferences = getSharedPreferences("PreferenceAidYou", 0);
        sharedPreferences.edit().putBoolean("ServiceOn", true).apply();


    }

    void startForegroundApi() {
        NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, "Foreground Service Channel AidYou", NotificationManager.IMPORTANCE_MIN);
        NotificationManager mgr = getSystemService(NotificationManager.class);
        mgr.createNotificationChannel(serviceChannel);

        Intent notificationIntent = new Intent(this, FallDetection.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(this, CHANNEL_ID).setContentTitle("Aiding You").setContentText("We are running for your safety").setTicker("Hello!").build();
        startForeground(10023, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForegroundApi();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        delay = sharedPreferences.getInt("delayTime",20)*1000;
        return super.onStartCommand(intent, flags, startId);
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
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onSensorChanged(SensorEvent event) {


        Sensor mySensor = event.sensor;
        if(mySensor.getType() == Sensor.TYPE_ACCELEROMETER){
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];

            curTime = System.currentTimeMillis();
            if((curTime-lastUpdate) > 100 ){


                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x+y+z - last_x - last_y - last_z)/diffTime * 10000;
                if(speed > SHAKE_THRESHOLD){

                    Toast.makeText(FallDetection.this,"Trigerred",Toast.LENGTH_SHORT).show();
                    timer = new AlteredTimer();
                    AlteredTimer.active =true;
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
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
                            AlteredTimer.active=false;
                            Log.i("Fall Detection Service","Message sent!");
                        }
                    }, 20000);
                }
                last_x=x;
                last_y=y;
                last_z=z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("FallDetection","Stopped");
        Toast.makeText(this,"Service Stopped",Toast.LENGTH_SHORT).show();
        sensorManager.unregisterListener(this);
        sharedPreferences.edit().putBoolean("ServiceOn",false).apply();
    }
}
