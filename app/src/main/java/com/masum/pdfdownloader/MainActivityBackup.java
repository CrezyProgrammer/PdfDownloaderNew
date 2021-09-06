package com.masum.pdfdownloader;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.masum.pdfdownloader.databinding.ActivityMainBinding;
import com.masum.pdfdownloader.databinding.DownloadDialogBinding;
import com.masum.pdfdownloader.services.ForegroundService;

import java.io.File;
import java.util.ArrayList;

public class MainActivityBackup extends AppCompatActivity implements  ItemClick {
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

        list=new ArrayList<Item>();
        list.add(new Item("Test Name", "https://jsoncompare.org/LearningContainer/SampleFiles/PDF/sample-pdf-with-images.pdf"));
        list.add(new Item("File Sample 150kb","https://file-examples-com.github.io/uploads/2017/10/file-sample_150kB.pdf"));
        list.add(new Item("Rahe Belayet","https://www.pixelapps.info/my_important_files/pdf_files/rahe_belayat.pdf"));

        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
       // binding.recyclerview.setAdapter(new RecyclerItemAdapter(list,this));

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
                    Toast.makeText(MainActivityBackup.this,"Fail to download",Toast.LENGTH_SHORT).show();
                }
                Boolean complete=intent.getBooleanExtra("complete",false);
                if (complete){
                    if (binding.recyclerview.getAdapter()!=null)  binding.recyclerview.getAdapter().notifyDataSetChanged();
                    isComplete=true;
                    if (dialog!=null &&dialog.isShowing())
                        dialog.dismiss();
                    Log.i("123321", "onReceive: File download complete");

                    File file=new File(intent.getStringExtra("path"));
                    if (file.exists())
                    {
                        if (isRunning&&dialog.isShowing()){
                            startActivity(
                                    new Intent(MainActivityBackup.this, MainActivity2.class).putExtra(
                                    "path",
                                    file.getAbsoluteFile()
                                    //  holder.main.context.applicationInfo.dataDir.toString() + "/book.pdf"
                                    )
                           );
                        }
                        else Toast.makeText(MainActivityBackup.this,"Download Complete",Toast.LENGTH_SHORT).show();
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
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );



        layoutBinding.bg.setOnClickListener(view -> dialog.dismiss());


        layoutBinding.cancel.setOnClickListener(view -> {
        Boolean b=ForegroundService.Companion.isDownload();
            Intent intent= new Intent(this, ForegroundService.class);
            stopService(intent);
            dialog.dismiss();
        });
        layoutBinding.progressBar.setMax(100);
}




    @Override
    public void itemClick(Item item) {

    }
}
