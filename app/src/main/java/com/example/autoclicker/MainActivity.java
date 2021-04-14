package com.example.autoclicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.androidhiddencamera.HiddenCameraFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {
    private static final int IGNORE_BATTERY_OPTIMIZATION_REQUEST = 1002;
    private static final String[] requiredPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.SET_ALARM,
            Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Manifest.permission.CAMERA,
    };
    Button start;
//    TextView textView;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start=(Button) findViewById(R.id.start);
//         textView=findViewById(R.id.selectedTime);
        checkPermissions();
//        textView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                TimePickerDialog mTimePicker;
//                Calendar mcurrentTime = Calendar.getInstance();
//               int  hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
//               int minute = mcurrentTime.get(Calendar.MINUTE);
//                mTimePicker = new TimePickerDialog(MainActivity.this, (timePicker, selectedHour, selectedMinute) -> {
//                    String format = "";
//                    if (selectedHour == 0) {
//                        selectedHour += 12;
//                        format = "AM";
//                    } else if (selectedHour == 12) {
//                        format = "PM";
//                    } else if (selectedHour > 12) {
//                        selectedHour -= 12;
//                        format = "PM";
//                    } else {
//                        format = "AM";
//                    }
//                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("settings",Context.MODE_PRIVATE);
//                    SharedPreferences.Editor editor = sharedPref.edit();
//                    editor.putInt("hour",selectedHour);
//                    editor.putInt("minute",selectedMinute);
//                    editor.putBoolean("am", format.equals("AM"));
//                    editor.apply();
//
//                    if (textView != null) {
//                        String hourS = String.valueOf(selectedHour < 10 ? "0" + selectedHour : selectedHour);
//                        String minS = String.valueOf(selectedMinute < 10 ? "0" + selectedMinute : selectedMinute);
//
//                        String text = "select time" + " " + hourS + " : " + minS + " " + format;
//                        textView.setText(text);
//                        textView.setVisibility(View.VISIBLE);
//                    }
//                }, hour, minute, true);//Yes 24 hour time
//                mTimePicker.setTitle("Select Time");
//                mTimePicker.show();
//
//            }
//        });
        start.setOnClickListener(view -> {
            Toast.makeText(this,
                    "started service",
                    Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
            intent.setAction("clicked");
            AlarmManager alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
            alarmMgr.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
        });
//        findViewById(R.id.close).setOnClickListener(view -> {
//            AlarmManager alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
//            Intent cameraService_intent = new Intent(this,Camera2Service.class);
//            cameraService_intent.putExtra("Front_Request", true);
//            PendingIntent alarmIntent =  PendingIntent.getService(this, 0, cameraService_intent, PendingIntent.FLAG_UPDATE_CURRENT );
//            alarmMgr.cancel(alarmIntent);
//        });
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_CODE: {
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    checkPermissions();
                }
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case IGNORE_BATTERY_OPTIMIZATION_REQUEST:
                if (!Settings.canDrawOverlays(this)) {

                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, 1);
                }
                break;
        }
    }
    /**
     * checking  permissions at Runtime.
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        final List<String> neededPermissions = new ArrayList<>();
        for (final String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    permission) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(permission);
            }
        }

        if (!neededPermissions.isEmpty()) {
            requestPermissions(neededPermissions.toArray(new String[]{}),
                    MY_PERMISSIONS_REQUEST_ACCESS_CODE);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivityForResult(intent,IGNORE_BATTERY_OPTIMIZATION_REQUEST);
            }
        }
    }
}


