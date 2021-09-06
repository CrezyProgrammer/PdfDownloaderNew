package com.masum.pdfdownloader

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.coolerfall.download.*
import com.masum.pdfdownloader.databinding.ActivityMainBinding
import com.masum.pdfdownloader.databinding.DownloadDialogBinding
import com.masum.pdfdownloader.others.Utils
import com.masum.pdfdownloader.services.ForegroundService
import java.io.File
import java.io.IOException
import java.net.URL
import java.net.URLConnection


class MainActivity : AppCompatActivity(),ItemClick{
    lateinit var binding: ActivityMainBinding
lateinit var list:ArrayList<Item>
var isRunning=false
    lateinit var layout:DownloadDialogBinding
    lateinit var dialog:Dialog
    lateinit var         mMessageReceiver: BroadcastReceiver
    var fileSize=0.0
    var isComplete=true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        layout= DownloadDialogBinding.inflate(layoutInflater)
        showDialog()

        list= ArrayList()
        list.add(Item("Test Name", "https://jsoncompare.org/LearningContainer/SampleFiles/PDF/sample-pdf-with-images.pdf"))
        list.add(Item("File Sample 150kb","https://file-examples-com.github.io/uploads/2017/10/file-sample_150kB.pdf"))
        list.add(Item("Rahe Belayet","https://www.pixelapps.info/my_important_files/pdf_files/rahe_belayat.pdf"))

        binding.recyclerview.layoutManager=LinearLayoutManager(this)
        binding.recyclerview.adapter=RecyclerItemAdapter(list,this)

     mMessageReceiver = object : BroadcastReceiver() {
       
            override fun onReceive(context: Context?, intent: Intent) {
                val current = intent.getDoubleExtra("timer", 0.0)
                val progress:Double=(current/(fileSize))*100
                if (dialog!=null&&layout!=null){
                    layout.progressBar.isIndeterminate=false
                    layout.progressText.text="$current Mb/${fileSize}Mb"
                    layout.progressBar.progress = progress.toInt()
                }


                val error:Boolean=intent.getBooleanExtra("error",false)
                if (error&&dialog!=null){
                    dialog.dismiss()
                    Toast.makeText(this@MainActivity,"Fail to download",Toast.LENGTH_SHORT).show()
                }
                val complete:Boolean=intent.getBooleanExtra("complete",false)
                if (complete){
                  if (binding.recyclerview.adapter!=null)  binding.recyclerview.adapter!!.notifyDataSetChanged()
                    isComplete=true
                    if (dialog!=null &&dialog.isShowing)
                        dialog.dismiss()
                    Log.i("123321", "onReceive: File download complete")

                    val file=File(intent.getStringExtra("path"))
                    if (file.exists())
                    {
                       if (isRunning&&dialog.isShowing){
                           startActivity(
                               Intent(this@MainActivity, MainActivity2::class.java).putExtra(
                                   "path",
                                   file.absolutePath
                                   //  holder.main.context.applicationInfo.dataDir.toString() + "/book.pdf"
                               )
                           )
                       }
                        else Toast.makeText(this@MainActivity,"Download Complete",Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    isComplete=false
                }






            }
        }


    }

    private fun showDialog() {
       dialog = Dialog(this)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)

        dialog?.setContentView(layout.root)
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        dialog.window!!.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )



    layout.bg.setOnClickListener {
        dialog.dismiss()
    }

        layout.cancel.setOnClickListener {
            ForegroundService.isDownload=false
          val intent=  Intent(this, ForegroundService::class.java)
            stopService(intent)
            dialog.dismiss()

        }
        layout.progressBar.max=100


    }

    override fun onStart() {
        isRunning=true
        val filter = IntentFilter()
        filter.addAction("timer_tracking")
        registerReceiver(mMessageReceiver,filter)
        super.onStart()
    }

    override fun onDestroy() {
        isRunning=false
        unregisterReceiver(mMessageReceiver)
        super.onDestroy()
    }

    override fun onResume() {
        isRunning=true
        super.onResume()
    }

    override fun onPause() {
       isRunning=false
        super.onPause()
    }

    override fun itemClick(item: Item) {


        val file=File(this.applicationInfo.dataDir.toString()+"/" +item.name+".pdf")
        if (file.exists()){
            startActivity(
                Intent(this, MainActivity2::class.java).putExtra(
                    "path",
                    file.absolutePath

                )
            )
        }
        else
        {
            Log.i("123321", "itemClick: ${ForegroundService.isDownload}")
            if (ForegroundService.isDownload) {
                if (dialog != null) {
                    dialog.show()
                }
            }
                else
                {
                    if (dialog!=null && !dialog.isShowing) {
                        layout.progressBar.isIndeterminate=true
                        layout.progressText.text="--Mb/--Mb"
                        dialog.show()
                    }
                    Thread {
                        try {
                            val myUrl = URL(item.link)
                            val urlConnection: URLConnection = myUrl.openConnection()
                            urlConnection.connect()
                            val file_size: Int = urlConnection.getContentLength()
                            val size = Utils.getBytesToMBString(file_size.toLong())
                            fileSize=size.toDouble()
                            val intent=  Intent(this, ForegroundService::class.java)
                            stopService(intent)


                            ForegroundService.startService(this@MainActivity, "${ item.name }.pdf",size.toDouble(),item.link)



                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }.start()

                }
            }



    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.i("123321", "onNewIntent: ")
        if (!isComplete&&dialog!=null)dialog.show()
    }



}