package com.masum.pdfdownloader.services


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.ListenableWorker
import com.coolerfall.download.*
import com.masum.pdfdownloader.MainActivityBackup
import com.masum.pdfdownloader.R
import com.masum.pdfdownloader.others.Utils
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit


class ForegroundService : Service() {
    private val CHANNEL_ID = "ForegroundServic"
    lateinit var        downloadManager:DownloadManager

    companion object {
            var isDownload=false
        fun startService(context: Context, name: String, size: Double, link: String) {

            val startIntent = Intent(context, ForegroundService::class.java)
            startIntent.putExtra("name",name)
            startIntent.putExtra("size",size)
            startIntent.putExtra("link",link)
            Log.i("123321", "startService: starting service")
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, ForegroundService::class.java)
            context.stopService(stopIntent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

      val name=intent?.getStringExtra("name")!!
        val size=intent?.getDoubleExtra("size",0.0)!!
        val link=intent?.getStringExtra("link")!!




        //do heavy work on a background thread

        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivityBackup::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Downloading File")
            .setAutoCancel(true)
            .setContentText("Wait some moment")
            .setContentIntent(pendingIntent)
            .build()

        Log.i("123321", "onStartCommand: creating forground")
        notify(name,"Download starting",1)

        startForeground(1, notification)
       downloadFile(name,size,link)
        //stopSelf();
        return START_STICKY
    }

    private fun downloadFile(name: String, size: Double, link: String) {



        val client: OkHttpClient = OkHttpClient.Builder().build()
   downloadManager  = DownloadManager.Builder().context(this)
            .downloader(OkHttpDownloader.create(client))
            .threadPoolSize(3)
            .logger { message -> Log.d("12332", message!!) }
            .build()


        val request: DownloadRequest = DownloadRequest.Builder().url(link)
            .downloadCallback(object : DownloadCallback() {
                override fun onStart(downloadId: Int, totalBytes: Long) {

                    Log.i("123321","165:start")
                }
                override fun onRetry(downloadId: Int) {}
                override fun onProgress(
                    downloadId: Int,
                    bytesWritten: Long,
                    totalBytes: Long
                ) {
                    isDownload=true
                    val current:Double= Utils.getBytesToMBString(bytesWritten).toDouble()
                    notify(name,
                        "Downloaded $current Mb/${size}Mb",
                        1
                    )

                    val intent = Intent("timer_tracking")
                    intent.putExtra("timer", current)
                  sendBroadcast(intent);



                }

                override fun onSuccess(downloadId: Int, filePath: String?) {
                    isDownload=false

                    Log.i("123321","153:"+filePath)
                    notify(name,"Download Complete", 1)

                    val intent = Intent("timer_tracking")
                    intent.putExtra("complete", true)
                    intent.putExtra("path",filePath)
                    sendBroadcast(intent);


                }



                override fun onFailure(downloadId: Int, statusCode: Int, errMsg: String?) {
                    isDownload=false
                    val intent = Intent("timer_tracking")
                    intent.putExtra("error", true)

                    sendBroadcast(intent);


                    Log.i("123321", "onFailure: $errMsg")

                    ListenableWorker.Result.failure()
                }
            })
            .retryTime(3)
            .retryInterval(3, TimeUnit.SECONDS)
            .progressInterval(1, TimeUnit.SECONDS)
            .priority( Priority.HIGH )
            .destinationFilePath(this.applicationInfo.dataDir.toString()+"/" +name)

            .build()

        downloadManager?.add(request)




    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }



    fun notify(title: String, message: String, id: Int){

        val notificationIntent = Intent(this, MainActivityBackup::class.java)
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this,CHANNEL_ID)
            .setContentText(message)
            .setContentTitle(title)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)




        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager


        mNotificationManager.notify(id, builder.build())
    }
    private fun createNotificationChannel() {
        Log.i("123321", "createNotificationChannel: ")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        downloadManager.cancelAll()
        super.onDestroy()
    }
}