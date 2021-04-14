package com.example.autoclicker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        scheduleAlarm(context);

    }

    void scheduleAlarm(Context context) {
        Log.i("service", "scheduleAlarm: " + "started service");
//        SharedPreferences sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
//        int hour = sharedPref.getInt("hour", 7);
//        int minute = sharedPref.getInt("minute", 0);
//        boolean am = sharedPref.getBoolean("am", true);
//        Toast.makeText(context,
//                "select time is" +hour+" "+minute+" "+am,
//                Toast.LENGTH_SHORT).show();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR,8);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.AM_PM, Calendar.AM);

//        if (am)
//            calendar.set(Calendar.AM_PM, Calendar.AM);
//        else
//            calendar.set(Calendar.AM_PM, Calendar.PM);

        Log.i("service", "scheduleAlarm: " + " inital alarm scheduled for" + calendar.getTime());
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent cameraService_intent = new Intent(context, Camera2Service.class);
        cameraService_intent.putExtra("Front_Request", true);

        if (calendar.getTimeInMillis() > System.currentTimeMillis()) {
            int id = (int) System.currentTimeMillis();
            //first alarm
            PendingIntent alarmIntent = PendingIntent.getService(context, id, cameraService_intent, 0);
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(), alarmIntent);
        } else {
            calendar.add(Calendar.DATE, 1);
            Log.i("service", "scheduleAlarm: " + "alarm rescheduled for" + calendar.getTime());
            int id = (int) System.currentTimeMillis();
            //first alarm
            PendingIntent alarmIntent = PendingIntent.getService(context, id, cameraService_intent, 0);
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(), alarmIntent);
        }
    }
}