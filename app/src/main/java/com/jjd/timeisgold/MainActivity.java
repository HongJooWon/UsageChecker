package com.jjd.timeisgold;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import android.app.AppOpsManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.app.AlertDialog;
import android.widget.ImageView;
import android.widget.Toast;


//firebase libraries
//import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.components.BuildConfig;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.analytics.FirebaseAnalytics;
//import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
//import com.google.firebase.remoteconfig.FirebaseRemoteConfigInfo;
//import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

//import com.google.android.gms.tasks.Task;

import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    // 파이어베이스 데이터베이스 연동
    private FirebaseAnalytics analytics;

    CheckPackageNameThread checkPackageNameThread;

    DBHelper dbHelper;


    private NotificationManagerCompat notificationManager;

    String[] items = {"5min","10min","15min","20min","25min","30min"};
    int[] times = {3,600,900,1200,1500,1800};
    int selectedTime;
    int nowTime=0;
    int totalTime = 0;
    String packinfo = "";

    private void createNotificationsChannels(){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(
                    "Notification",
                    "Notification",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }


    //Notification Method
    private void displayNotification(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"Notification")
                .setSmallIcon(R.drawable.gold)
                .setContentTitle("From TIG")
                .setContentText("You should stop Using your Phone!")
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setVibrate(new long[]{0,500,250,500,250,1000});

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(MainActivity.this);
        managerCompat.notify(1,builder.build());
    }

    //Alert Dialog Function
    void showDialog() {
        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(MainActivity.this)
                .setTitle("TimeIsGold")
                .setMessage("You Should Stop Using Your Phone")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        AlertDialog msgDlg = msgBuilder.create();
        msgDlg.show();
    }


    boolean operation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Firebase A/B testing  call
//        FirebaseRemoteConfig mfirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
//        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
//                .setMinimumFetchIntervalInSeconds(0)
//                        .build();
//
//        mfirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
//        mfirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
//        mfirebaseRemoteConfig.fetchAndActivate()
//                .addOnCompleteListener(new OnCompleteListener() {
//                    @Override
//                    public void onComplete(@NonNull Task task) {
//                        if(task.isSuccessful()) {
//                            boolean updated = task.isSuccessful();
//                            Log.d("remote config", "updated: " + updated);
//                        } else {
//                            Log.d("remote config", "fetched failed");
//
//                        }
//
//                        FirebaseRemoteConfig mfirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
//                        long version = mfirebaseRemoteConfig.getLong("version_number");
//                        Log.d("remote config", "version:"+version);
//                        updateUI((int)version);
//
//                    }
//                });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notificationManager = NotificationManagerCompat.from(this);

        createNotificationsChannels();

        //create database object
        dbHelper = new DBHelper(MainActivity.this, 1);

        //get firebase instanceID
        analytics = FirebaseAnalytics.getInstance(this);


        //help button
        Button helpBtn = (Button)findViewById(R.id.button_help);

        helpBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                helpDialog(view);
            }
        });

        // Statistics button
        Button sbtn = (Button)findViewById(R.id.sbtn);

        // if click, graphactivity page appears
        sbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get the most used app name from database
                packinfo = dbHelper.getMost();
                Log.v("pack name", packinfo);

                //go to the graph activity
                Intent myIntent = new Intent(MainActivity.this,GraphActivity.class);
                startActivity(myIntent);
            }

            //go the graph activity
        });

        Switch sw = (Switch)findViewById(R.id.sw);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Bundle bundle = new Bundle(); // logEvent()까지 추가

                //if the switch is true
                if(isChecked){
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "switch on");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Switch handling");
                    analytics.logEvent("Switch_ON", bundle);

                    if(!checkPermission()) {
                        Intent PermissionIntent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS, Uri.parse("package:" + getPackageName()));
                        startActivity(PermissionIntent);
                    }
                    // if there is an accept of permission proceed the thread of UsageStatManager
                    else{

                        //SCREEN_INTERACTIVE
                        operation = true;
                        checkPackageNameThread = new CheckPackageNameThread();
                        checkPackageNameThread.start();
                    }
                    //Show coin image
                    ImageView v2_image2 = (ImageView)findViewById(R.id.image_coin);
                    if(v2_image2.getVisibility()==View.INVISIBLE){
                        v2_image2.setVisibility(View.VISIBLE);
                    }

                //if the switch is off
                }else {
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "switch off");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Switch handling");
                    analytics.logEvent("Switch_Off", bundle);
                    operation = false;
                    //coin image disappears
                    ImageView v2_image2 = (ImageView)findViewById(R.id.image_coin);
                    if(v2_image2.getVisibility()==View.VISIBLE){
                        v2_image2.setVisibility(View.INVISIBLE);
                    }
                    nowTime=0;
                }
            }
        });

    }


    // function that detects the foreground app package every 3 second
    public class CheckPackageNameThread extends Thread{

        public void run(){
            // work when operation == true
            while(operation){
                if(!checkPermission())
                    continue;



                try {
                    // check app package every 3 second
                    //화면 사용하지 않을 경우 실행 정지
                    if(isScreenOn()){
                        Log.v("Usage", "Using");
                        Log.v("time is", "Time is"+nowTime);
                        operation = true;
                    } else {
                        Log.v("Usage", "Not Using");
                        operation = false;
                    }
                        sleep(3000);
                        nowTime +=3;
                        //insert time data to data base
                        dbHelper.Update(getPackageName(MainActivity.this));
                        if(nowTime == selectedTime){
                            //alarm and allocate nowTime to 0;
                            displayNotification();
                            //if time is up, proceed the function
                            runOnUiThread(new Runnable(){
                                @Override
                                public void run() {
                                    showDialog();
                                }
                            });
                            nowTime=0;
                        }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    // 권한 체크
    private boolean checkPermission(){

        boolean granted = false;

        AppOpsManager appOps = (AppOpsManager) getApplicationContext()
                .getSystemService(Context.APP_OPS_SERVICE);

        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getApplicationContext().getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (getApplicationContext().checkCallingOrSelfPermission(
                    android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        }
        else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }

        return granted;
    }


    // fuction that gets foreground app package name
    public static String getPackageName(@NonNull Context context) {

        //create UsageStatsManager
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        //last app timestamp
        long lastRunAppTimeStamp = 0L;

        // fixed time as the user set
//        long user_time = 0;

        // To determine how long to get the name of the app you collected (collect the name of the app from begin to end)
        final long INTERVAL = 1000 * 60 * 3;
        final long end = System.currentTimeMillis();
        final long begin = end - INTERVAL;


        LongSparseArray packageNameMap = new LongSparseArray<>();

        // UsageEvents to contain collected events
        final UsageEvents usageEvents = usageStatsManager.queryEvents(begin, end);

        // If there are multiple events (asNextEvent should be at least present, since it is not null)
        while (usageEvents.hasNextEvent()) {

            // Get the current event
            UsageEvents.Event event = new UsageEvents.Event();
            usageEvents.getNextEvent(event);

            // If the current event is foreground (if the app is currently on the screen)
            if(isForeGroundEvent(event)) {

                // Put the app name in the packageNameMap.
                packageNameMap.put(event.getTimeStamp(), event.getPackageName());

                // Updates the timestamp for the most recently executed event.
                if(event.getTimeStamp() > lastRunAppTimeStamp) {
                    lastRunAppTimeStamp = event.getTimeStamp();
                }
            }
        }
        // Returns the name of the last app.
        return packageNameMap.get(lastRunAppTimeStamp, "").toString();
    }


    public boolean isScreenOn() {

        DisplayManager dm = (DisplayManager) this.getSystemService(Context.DISPLAY_SERVICE);
        for (Display display : dm.getDisplays()) {
            if (display.getState() != Display.STATE_OFF) {
                return true;
            }
        }
        return false;
    }

    // check app is foreground or not
    private static boolean isForeGroundEvent(UsageEvents.Event event) {

        // if there is no event return false
        if(event == null)
            return false;

        // if event is foreground return true
        if(BuildConfig.VERSION_CODE >= 29)
            return event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED;

        return event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND;
    }

    // method to change version of application for A/B testing
//    private void updateUI( int versionCode){
//        if (versionCode == 2) {
//           TextView v1_text1 = (TextView)findViewById(R.id.text_every);
//           TextView v1_text2 = (TextView)findViewById(R.id.text_time);
//           TextView v1_text3 = (TextView)findViewById(R.id.text_save);
//           TextView v1_text4 = (TextView)findViewById(R.id.text_on);
//           TextView v1_text5 = (TextView)findViewById(R.id.text_off);
//           Spinner v1_spinner = (Spinner)findViewById(R.id.spinner);
//           v1_text1.setVisibility(View.GONE);
//           v1_text2.setVisibility(View.GONE);
//           v1_text3.setVisibility(View.GONE);
//           v1_text4.setVisibility(View.GONE);
//           v1_text5.setVisibility(View.GONE);
//           v1_spinner.setVisibility(View.GONE);
//
//           Button v2_btn = (Button)findViewById(R.id.button_help);
//           ImageView v2_image1 = (ImageView)findViewById(R.id.image_indicator);
//           ImageView v2_image2 = (ImageView)findViewById(R.id.image_coin);
//           v2_btn.setVisibility(View.VISIBLE);
//           v2_image1.setVisibility(View.VISIBLE);
//           v2_image2.setVisibility(View.INVISIBLE);
//        }
//    }
    // time change method
    public void changeTime(View view){
        AlertDialog.Builder timeDialog = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        timeDialog.setTitle("Choose your term to alarm youself ").setItems(items, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                Toast.makeText(getApplicationContext(), items[which], Toast.LENGTH_LONG).show();
                selectedTime = times[Arrays.asList(items).indexOf(items[which])];
        }
    })
                .setCancelable(false)
                .show();
    }
    // to show help message for help button
    public void helpDialog(View view){
        AlertDialog.Builder helpDlog = new AlertDialog.Builder(this);
        helpDlog.setTitle("How to Use").setMessage("First\n" +
                "Touch the indicator icon\n" +
                "and select the time that you want to alarm\n" +
                "\n"+
                "Second\n" +
                "turn the switch to red side\n" +
                "\n"+
                "Third\n" +
                "this application will alarm you every time you select\n" +
                "while you using your smartphone\n" +
                "So that you can handle and manage your time and be free from smartphone\n" +
                "MAKE YOUR TIME WORTHY WITH THIS APP\n" +
                "\n"+
                "Tim is Gold!");
        AlertDialog alertDialog = helpDlog.create();
        alertDialog.show();
    }
}