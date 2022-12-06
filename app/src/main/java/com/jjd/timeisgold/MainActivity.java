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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.app.AlertDialog;


//파이어베이스 메소드 라이브러리
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.components.BuildConfig;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigInfo;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import com.google.android.gms.tasks.Task;


public class MainActivity extends AppCompatActivity {

    // 파이어베이스 데이터베이스 연동
    private FirebaseAnalytics analytics;

    CheckPackageNameThread checkPackageNameThread;

    DBHelper dbHelper;

    private Context mContext;
    //Notification Channel
//    private static String CHANNEL_ID;
//    private static final String CHANNEL_NAME = "Time Notification";
//    private static final String CHANNEL_DESC = "Time Notification";
    private NotificationManagerCompat notificationManager;

    String[] items = {"5min","10min","15min","20min","25min","30min"};
    int[] times = {300,600,900,1200,1500,1800};
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
                .setAutoCancel(true);

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
                        finish();
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

        //파이어베이스 a/b 테스트 객체
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

        //데이터 베이스 오브젝트 생성
        dbHelper = new DBHelper(MainActivity.this, 1);
        analytics = FirebaseAnalytics.getInstance(this);





        // 버튼 정의
        //Button start_button = findViewById(R.id.start_button);
        Button sbtn = (Button)findViewById(R.id.sbtn);

        //통계 버튼을 누르면 DB접근
        sbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //에딧 텍스트 값을 문자열로 바꾸어 함수에 넣어줍니다.
                packinfo = dbHelper.getMost();
                Log.v("pack name", packinfo);

                //go to the graph activity
                Intent myIntent = new Intent(MainActivity.this,GraphActivity.class);
                startActivity(myIntent);
                finish();
            }

            //go the graph activity
        });

        Switch sw = (Switch)findViewById(R.id.sw);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Bundle bundle = new Bundle(); // logEvent()까지 추가


                if(isChecked){
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "switch on");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Switch handling");
                    analytics.logEvent("Switch_ON", bundle);

                    if(!checkPermission()) {
                        Intent PermissionIntent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS, Uri.parse("package:" + getPackageName()));
                        startActivity(PermissionIntent);
                    }
                    // 권환 허용 되어 있으면 현재 포그라운드 앱 패키지 로그로 띄운다.
                    else{

                        //SCREEN_INTERACTIVE
                        operation = true;
                        checkPackageNameThread = new CheckPackageNameThread();
                        checkPackageNameThread.start();
                    }
                }else {
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "switch off");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Switch handling");
                    analytics.logEvent("Switch_Off", bundle);
                    operation = false;
                }
            }
        });

        // 시작 버튼 이벤트
//            start_button.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                    // 권환 허용이 안되어 있으면 권환 설정창으로 이동
//                    if(!checkPermission()) {
//                        Intent PermissionIntent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS, Uri.parse("package:" + getPackageName()));
//                        startActivity(PermissionIntent);
//                    }
//                    // 권환 허용 되어 있으면 현재 포그라운드 앱 패키지 로그로 띄운다.
//                    else{
//                        //SCREEN_INTERACTIVE
//                        operation = true;
//                        checkPackageNameThread = new CheckPackageNameThread();
//                        checkPackageNameThread.start();
//                    }
//
//                }
//            });

        // 종료 버튼 이벤트
//        end_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                operation = false;
//            }
    //});

        //Spinner UI set
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,items
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void  onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedTime =times[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedTime =times[0];
            }
        });

    }






    // 현재 포그라운드 앱 패키지 로그로 띄우는 함수
    public class CheckPackageNameThread extends Thread{

        public void run(){
            // operation == true 일때만 실행
            while(operation){
                if(!checkPermission())
                    continue;


                // 현재 포그라운드 앱 패키지 이름 가져오기
//                System.out.println(getPackageName(getApplicationContext()));

                try {
                    // 5초마다 패키치 이름을 로그창에 출력
                    
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
                        //지정 시간마다 데이터 베이스 업데이트
                        dbHelper.Update(getPackageName(MainActivity.this));
                        if(nowTime == selectedTime){
                            //alarm and allocate nowTime to 0;
                            displayNotification();
                            //여기에 작동시킬 함수 넣기
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


    // 자신의 앱의 최소 타겟을 롤리팝 이전으로 설정
    // 현재 포그라운드 앱 패키지를 가져오는 함수
    public static String getPackageName(@NonNull Context context) {

        // UsageStatsManager 선언
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        // 마지막 실행 앱 타임스탬프
        long lastRunAppTimeStamp = 0L;

        // fixed time as the user set
//        long user_time = 0;

        // 얼마만큼의 시간동안 수집한 앱의 이름을 가져오는지 정하기 (begin ~ end 까지의 앱 이름을 수집한다)
        final long INTERVAL = 1000 * 60 * 3; // 여기다 시간 분단위 user_time로 받아서 집어넣기
        final long end = System.currentTimeMillis();
        final long begin = end - INTERVAL; // 5분전


        LongSparseArray packageNameMap = new LongSparseArray<>();

        // 수집한 이벤트들을 담기 위한 UsageEvents
        final UsageEvents usageEvents = usageStatsManager.queryEvents(begin, end);

        // 이벤트가 여러개 있을 경우 (최소 존재는 해야 hasNextEvent가 null이 아니니까)
        while (usageEvents.hasNextEvent()) {

            // 현재 이벤트를 가져오기
            UsageEvents.Event event = new UsageEvents.Event();
            usageEvents.getNextEvent(event);

            // 현재 이벤트가 포그라운드 상태라면(현재 화면에 보이는 앱이라면)
            if(isForeGroundEvent(event)) {

                // 해당 앱 이름을 packageNameMap에 넣는다.
                packageNameMap.put(event.getTimeStamp(), event.getPackageName());

                // 가장 최근에 실행 된 이벤트에 대한 타임스탬프를 업데이트 해준다.
                if(event.getTimeStamp() > lastRunAppTimeStamp) {
                    lastRunAppTimeStamp = event.getTimeStamp();
                }
            }
        }
        // 가장 마지막까지 있는 앱의 이름을 리턴해준다.
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

    // 앱이 포그라운드 상태인지 체크
    private static boolean isForeGroundEvent(UsageEvents.Event event) {

        // 이벤트가 없으면 false 반환
        if(event == null)
            return false;

        // 이벤트가 포그라운드 상태라면 true 반환
        if(BuildConfig.VERSION_CODE >= 29)
            return event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED;

        return event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND;
    }

}