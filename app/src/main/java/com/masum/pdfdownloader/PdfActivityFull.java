package com.masum.pdfdownloader;


import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.NumberPicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.masum.pdfdownloader.databinding.ActivityPdfBinding;

import java.io.File;


public class PdfActivityFull extends AppCompatActivity {
    SharedPreferences preference;
    private  Boolean isNightMode=false;
    private  Boolean isVertical=true;
    private  Boolean isFullScreen=false;
    private ActivityPdfBinding binding;
    private  Boolean isHide=false;
    Handler handler;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityPdfBinding.inflate(getLayoutInflater());



            Log.i("123321",isFullScreen.toString());
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(binding.getRoot());

        isFullScreen=true;

        preference=getSharedPreferences("mode", MODE_PRIVATE);
        binding.fullScreen.setImageDrawable(ContextCompat.getDrawable(this,isFullScreen?R.drawable.ic_baseline_fullscreen_24:R.drawable.ic_baseline_fullscreen_exit_24));

        isNightMode=preference.getBoolean("night",false);
        isVertical=preference.getBoolean("mode",true);
        clickEvents();
        checkNightMode();
        modeSelection();


        Log.i("123321","page:"+getIntent().getIntExtra("current_page",0));

        loadPdf(getIntent().getIntExtra("current_page",0));

    }

    private void clickEvents() {
        binding.pdfView.setOnClickListener(view -> hide());
        binding.first.setOnClickListener(view -> {

            {  loadPdf(0);}
        });

        binding.fullScreen.setOnClickListener(view -> {


            startActivity(new Intent(this, PdfActivity.class)
            .putExtra("path",getIntent().getStringExtra("path"))

                    .putExtra("page",binding.pdfView.getCurrentPage())
            );
            finish();

        });

        binding.previous.setOnClickListener(view ->  { loadPdf(binding.pdfView.getCurrentPage()-1);});
        binding.forward.setOnClickListener(view ->  { loadPdf(binding.pdfView.getCurrentPage()+1) ;});
        binding.mode.setOnClickListener(view ->  {

            modeSelection();
            loadPdf(binding.pdfView.getCurrentPage());

        });

        binding.last.setOnClickListener (view -> { loadPdf(binding.pdfView.getPageCount()) ;});

        binding.pageSelection.setOnClickListener(view -> {
            if (handler!=null)handler.removeCallbacksAndMessages(null);
                    NumberPicker numberPicker = new NumberPicker(this);
                    //   changeDividerColor(numberPicker, Color.parseColor("#20000000"));
                    numberPicker.setMaxValue(binding.pdfView.getPageCount());
                    numberPicker.setMinValue(0);
                    numberPicker.setValue(binding.pdfView.getCurrentPage());


                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setView(numberPicker);
                    builder.setTitle("Select Page");
                    builder.setPositiveButton("OK", (dialogInterface, i) -> loadPdf(numberPicker.getValue()));


                    builder.create();
                    builder.show();
                });
        binding.nightMode.setOnClickListener(view -> {
            isNightMode=!isNightMode;
            SharedPreferences.Editor editor=getSharedPreferences("mode", MODE_PRIVATE).edit();
            editor.putBoolean("night",isNightMode);
            editor.apply();

            Log.i("123321","mode is night="+isNightMode);

            checkNightMode();

            loadPdf(binding.pdfView.getCurrentPage());

        });
    }

    private void modeSelection() {
        isVertical=!isVertical;
        SharedPreferences.Editor editor=getSharedPreferences("mode", MODE_PRIVATE).edit();
        editor.putBoolean("mode",preference.getBoolean("mode",isVertical));
        editor.apply();
        binding.mode.setRotation(binding.mode.getRotation()+90);
    }

    private void checkNightMode() {
        Drawable nightImage= ContextCompat.getDrawable(this,R.drawable.ic_night_mode);
        Drawable dayImage= ContextCompat.getDrawable(this,R.drawable.ic_sun);
        binding.nightMode.setImageDrawable(isNightMode?dayImage:nightImage);
        binding.pdfView.setBackgroundColor(isNightMode?Color.BLACK:Color.WHITE);
        int dayColor=ContextCompat.getColor(this,R.color.purple_500);
        int nightColor=ContextCompat.getColor(this,R.color.black);
        binding.toolbar3.setBackgroundColor(isNightMode?nightColor:dayColor);
        binding.toolbar4.setBackgroundColor(isNightMode?nightColor:dayColor);

    }

    private void loadPdf(int i) {
        hide();
        binding.progress.setVisibility(View.VISIBLE);
        if (preference != null) {
            String path=getIntent().getStringExtra("path");
            if (path==null){
                Log.i("123321","path is null");
            }
     else {
                File file=new File(path);
                binding.pdfView.fromFile(file).enableSwipe(true)
                        .defaultPage(i)
                        .onLoad(nbPages -> binding.progress.setVisibility(View.GONE))
                        .swipeHorizontal(isVertical)
                        .nightMode(isNightMode)
                        .load();
            }
        }

        }

        private void hide(){
        if (handler!=null)handler.removeCallbacksAndMessages(null);
        isHide=false;
        binding.toolbar3.setVisibility(View.VISIBLE);
        binding.toolbar4.setVisibility(View.VISIBLE);
        binding.pageSelection.setVisibility(View.VISIBLE);

        handler=new Handler();
        handler.postDelayed((Runnable) () -> {
            isHide=true;
            binding.toolbar3.setVisibility(View.GONE);
            binding.toolbar4.setVisibility(View.GONE);
            binding.pageSelection.setVisibility(View.GONE);
        },3000);

        }

    @Override
    public void onBackPressed() {
        if (isHide)hide();
        else super.onBackPressed();
    }
}