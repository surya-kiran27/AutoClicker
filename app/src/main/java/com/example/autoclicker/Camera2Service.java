package com.example.autoclicker;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;


public class Camera2Service extends Service {
    protected static final int CAMERA_CALIBRATION_DELAY = 500;
    protected static final String TAG = "myLog";
    protected static final int CAMERACHOICE = CameraCharacteristics.LENS_FACING_BACK;
    protected static long cameraCaptureStartTime;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession session;
    protected ImageReader imageReader;

    protected CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "CameraDevice.StateCallback onOpened");
            cameraDevice = camera;
            actOnReadyCameraDevice();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.w(TAG, "CameraDevice.StateCallback onDisconnected");
            stopSelf();

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "CameraDevice.StateCallback onError " + error);
            stopSelf();

        }
    };

    protected CameraCaptureSession.StateCallback sessionStateCallback = new CameraCaptureSession.StateCallback() {

        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        public void onReady(CameraCaptureSession session) {

        }


        @Override
        public void onConfigured(CameraCaptureSession session) {
            Camera2Service.this.session = session;
            try {
                session.capture(createCaptureRequest(), null, null);
                cameraCaptureStartTime = System.currentTimeMillis();
            } catch (CameraAccessException e) {
                Log.e(TAG, e.getMessage());

            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
        }
    };

    protected ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image img = reader.acquireLatestImage();
            processImage(img);


        }
    };

    public void readyCamera() {
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String pickedCamera = getCamera(manager);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            manager.openCamera(pickedCamera, cameraStateCallback, null);
            imageReader = ImageReader.newInstance(1920, 1080, ImageFormat.JPEG, 1 /* images buffered */);
            imageReader.setOnImageAvailableListener(onImageAvailableListener, null);
            Log.d(TAG, "imageReader created");
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
        }
    }
    private void startForegroundService() {

        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =new  NotificationChannel("com.example.autoclicker", "cam_service_channel_name", NotificationManager.IMPORTANCE_NONE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this)
                //.setSmallIcon(R.mipmap.app_icon)
                .setContentTitle("Background Video Recorder")
                .setContentText("")
                .setChannelId("com.example.autoclicker")
                .setContentIntent(pendingIntent).build();
        startForeground(6660,notification);
        return;
    }
    public String getCamera(CameraManager manager) {
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cOrientation == CAMERACHOICE) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand flags " + flags + " startId " + startId);

        readyCamera();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate service");
        startForegroundService();
        super.onCreate();
    }

    public void actOnReadyCameraDevice() {
        try {
            cameraDevice.createCaptureSession(Arrays.asList(imageReader.getSurface()), sessionStateCallback, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        if (null!=session)
            session.close();
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
        Log.i("exitservice", "exitService: "+"inside on destroy");

        exitService();
    }
    public  void exitService(){
        Log.i("exitservice", "exitService: "+"called");
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);

        intent.setAction("clicked");
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

        AlarmManager alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

        alarmMgr.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
    }

    private void processImage(Image image) {
        //Process image data
        ByteBuffer buffer;
        byte[] bytes;
        boolean success = false;
        Log.i(TAG, "processImage: "+"image clicked");
        boolean createFolderSuccess=false;
        try{

            File createFolder = new File(getApplicationContext().getExternalFilesDir(null),"autoClicker");

            if(!createFolder.exists())
                createFolderSuccess=   createFolder.mkdirs();

            String TimeStamp=getCurrentTimeStamp();
            File saveImage = new File(createFolder,TimeStamp+".jpg");
            FileOutputStream output = null;

            if (image.getFormat() == ImageFormat.JPEG) {
                buffer = image.getPlanes()[0].getBuffer();
                bytes = new byte[buffer.remaining()]; // makes byte array large enough to hold image
                buffer.get(bytes); // copies image from buffer to byte array
                try {
                    output  = new FileOutputStream(saveImage);
                    output.write(bytes);
                    output.flush();
                    output.close();
                    success = true;
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    image.close(); // close this to free up buffer for other images
                }


            }
        }catch (Exception e){
            e.printStackTrace();
        }

        Log.i(TAG, "processImage: "+"image clicked result :"+success+createFolderSuccess);

        stopSelf();


    }
    public static String getCurrentTimeStamp(){
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateTime = dateFormat.format(new Date()); // Find todays date

            return currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }
    protected CaptureRequest createCaptureRequest() {
        try {
            CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            builder.set(CaptureRequest.JPEG_ORIENTATION, this.getResources().getConfiguration().orientation);

            builder.addTarget(imageReader.getSurface());

            return builder.build();
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}