package com.masum.pdfdownloader;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.masum.pdfdownloader.databinding.ActivityMainBinding;
import com.masum.pdfdownloader.databinding.DownloadDialogBinding;
import com.masum.pdfdownloader.others.Utils;
import com.masum.pdfdownloader.services.ForgroundService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements  ItemClick {
    ActivityMainBinding binding;
    ArrayList<Item>list;
    Boolean isRunning=false;
    DownloadDialogBinding layoutBinding;
    Dialog dialog;
    BroadcastReceiver mMessageReceiver;
    Double fileSize=0.0;
    Boolean isComplete=true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        layoutBinding=DownloadDialogBinding.inflate(getLayoutInflater());
        showDialog();

        list= new ArrayList<>();
        list.add(new Item("Test Name", "https://jsoncompare.org/LearningContainer/SampleFiles/PDF/sample-pdf-with-images.pdf"));
        list.add(new Item("File Sample 150kb","https://file-examples-com.github.io/uploads/2017/10/file-sample_150kB.pdf"));
        list.add(new Item("Rahe Belayet","https://www.pixelapps.info/my_important_files/pdf_files/rahe_belayat.pdf"));

        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerview.setAdapter(new RecyclerItemAdapter(list,this));

        mMessageReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Double current = intent.getDoubleExtra("timer", 0.0);
                int progress= (int) ((current/(fileSize))*100);

                if (dialog!=null&&layoutBinding!=null){
                    layoutBinding.progressBar.setIndeterminate(false);
                    layoutBinding.progressText.setText(current+" Mb/"+fileSize+"Mb");
                    layoutBinding.progressBar.setProgress(progress);
                }

                Boolean error=intent.getBooleanExtra("error",false);
                if (error&&dialog!=null){
                    dialog.dismiss();
                    Toast.makeText(MainActivity.this,"Fail to download",Toast.LENGTH_SHORT).show();
                }
                Boolean complete=intent.getBooleanExtra("complete",false);
                if (complete){
                    if (binding.recyclerview.getAdapter()!=null)  binding.recyclerview.getAdapter().notifyDataSetChanged();
                    isComplete=true;


                    File file=new File(ForgroundService.path);
                    if (file.exists())
                    {
                        if (isRunning&&dialog.isShowing()){
                            if (dialog!=null &&dialog.isShowing())
                                dialog.dismiss();

                            startActivity(
                                    new Intent(MainActivity.this, PdfActivity.class).putExtra(
                                    "path",
                                    file.getAbsolutePath()
                                    )

                           );
                        }
                        else Toast.makeText(MainActivity.this,"Download Complete",Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    isComplete=false;
                }



            }
        };

    }

    private void showDialog() {
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setContentView(layoutBinding.getRoot());
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );



        layoutBinding.bg.setOnClickListener(view -> dialog.dismiss());


        layoutBinding.cancel.setOnClickListener(view -> {
       ForgroundService.isDownload=false;
            Intent intent= new Intent(MainActivity.this, ForgroundService.class);
            stopService(intent);
            dialog.dismiss();
        });
        layoutBinding.progressBar.setMax(100);
}



    @Override
   public void itemClick(Item item) {
        Log.i("123321","item click");


        File file=new File(this.getApplicationInfo().dataDir +"/" +item.name+".pdf");
        if (file.exists()){
            startActivity(
                    new Intent(this, PdfActivity.class).putExtra(
                    "path",
                    file.getAbsolutePath()

            )
            );
        }
        else
        {
            if (ForgroundService.isDownload) {
                if (dialog != null) {
                    dialog.show();
                }
            }
            else
            {
                if (dialog!=null && !dialog.isShowing()) {
                    layoutBinding.progressBar.setIndeterminate(true);
                    layoutBinding.progressText.setText("--Mb/--Mb");
                    dialog.show();

                }
              new  Thread(() -> {

                  try {
                      URL myUrl =new URL(item.link);
                      URLConnection urlConnection = myUrl.openConnection();
                      urlConnection.connect();
                      int file_size = urlConnection.getContentLength();
                      String size = Utils.getBytesToMBString(file_size);
                      fileSize=Double.parseDouble(size);

                      Intent intent= new Intent(MainActivity.this,ForgroundService.class);
                   //   stopService(intent);


                      Intent startIntent = new Intent(MainActivity.this, ForgroundService.class);
                      startIntent.putExtra("name",item.getName()+".pdf");
                      startIntent.putExtra("size",fileSize);
                      startIntent.putExtra("link",item.getLink());
                      Log.i("123321", "startService: starting service");
                      ContextCompat.startForegroundService(MainActivity.this, startIntent);
                  }
                  catch ( IOException e) {
                      e.printStackTrace();
                  }
              }) {


            }.start();

            }
        }



    }

    @Override
    public void onNewIntent(Intent intent)
    {
        Log.i("123321", "onNewIntent: "+isComplete);
        super.onNewIntent(intent);
        if (!isComplete&&dialog!=null)dialog.show();
    }

    @Override
    protected void onStart() {
        isRunning=true;

        IntentFilter filter =new  IntentFilter();
        filter.addAction("timer_tracking");
        registerReceiver(mMessageReceiver,filter);
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        isRunning=false;
        unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        isRunning=true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        isRunning=false;
        super.onPause();
    }
}
