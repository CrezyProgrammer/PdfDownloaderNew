package com.masum.pdfdownloader.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.coolerfall.download.DownloadCallback;
import com.coolerfall.download.DownloadManager;
import com.coolerfall.download.DownloadRequest;
import com.coolerfall.download.OkHttpDownloader;
import com.coolerfall.download.Priority;
import com.masum.pdfdownloader.MainActivity;
import com.masum.pdfdownloader.R;
import com.masum.pdfdownloader.others.Utils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class ForgroundService extends Service {
    private String CHANNEL_ID = "ForegroundServic";
    public static  String path="";
    public static boolean isDownload=false;
   DownloadManager downloadManager;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String name=intent.getStringExtra("name");
                Double size=intent.getDoubleExtra("size",0.0);
                String link=intent.getStringExtra("link");




                //do heavy work on a background thread

                createNotificationChannel();
        Intent notificationIntent =new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0, notificationIntent, 0
        );
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        Notification notification =new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Downloading File")
                .setAutoCancel(true)
                .setContentText("Wait some moment")
                .setContentIntent(pendingIntent)
                .build();

        notifyUser(name,"Download starting",1);

        startForeground(1, notification);
        downloadFile(name,size,link);

        return START_STICKY;
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
           NotificationChannel serviceChannel =new  NotificationChannel(
                    CHANNEL_ID, "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
    private void notifyUser(String title,String message,int id){

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0, notificationIntent, 0
        );
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentText(message)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true);




        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        mNotificationManager.notify(id, builder.build());
    }
    private void downloadFile(String name,Double size,String  link) {



        OkHttpClient client =new OkHttpClient.Builder().build();
        downloadManager  =new DownloadManager.Builder().context(this)
                .downloader(OkHttpDownloader.create(client))
                .threadPoolSize(3)
            .build();


        DownloadRequest request = new DownloadRequest.Builder().url(link)
                .downloadCallback(new DownloadCallback() {
                    @Override
                    public void onStart(int downloadId, long totalBytes) {
                        super.onStart(downloadId, totalBytes);
                    }

                    @Override
                    public void onRetry(int downloadId) {
                        super.onRetry(downloadId);
                    }

                    @Override
                    public void onProgress(int downloadId, long bytesWritten, long totalBytes) {
                        isDownload=true;
                        Double current= Double.valueOf(Utils.getBytesToMBString(bytesWritten));
                        notifyUser(name,
                                "Downloaded "+current+" Mb/"+size+"Mb",
                                1
                        );

                        Intent intent =new Intent("timer_tracking");
                        intent.putExtra("timer", current);
                        sendBroadcast(intent);
                    }

                    @Override
                    public void onSuccess(int downloadId, String filePath) {
                        isDownload=false;

                        Log.i("123321","153:"+filePath);
                        notifyUser(name,"Download Complete", 1);
                        path=filePath;
                        Intent intent =new Intent("timer_tracking");
                        intent.putExtra("complete", true);
                        intent.putExtra("path",filePath);
                        sendBroadcast(intent);
                    }

                    @Override
                    public void onFailure(int downloadId, int statusCode, String errMsg) {
                        isDownload=false;
                        Intent intent =new Intent("timer_tracking");
                        intent.putExtra("error", true);

                        sendBroadcast(intent);


                        Log.i("123321", "onFailure: $errMsg");

                    }






                })
            .retryTime(3)
                .retryInterval(3, TimeUnit.SECONDS)
                .progressInterval(1, TimeUnit.SECONDS)
                .priority( Priority.HIGH )
                .destinationFilePath(this.getApplicationInfo().dataDir +"/" +name)

                .build();

        downloadManager.add(request);




    }

    @Override
    public void onDestroy() {
        downloadManager.cancelAll();
        super.onDestroy();
    }
}
