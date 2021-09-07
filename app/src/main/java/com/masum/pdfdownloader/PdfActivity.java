package com.masum.pdfdownloader;


import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.masum.pdfdownloader.databinding.ActivityPdfBinding;

import java.io.File;


public class PdfActivity extends AppCompatActivity {
    SharedPreferences preference;
private final String TAG ="123321";
private ActivityPdfBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityPdfBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preference=getSharedPreferences("mode", MODE_PRIVATE);
        binding.first.setOnClickListener(view -> {
            {  loadpdf(0);}
        });
        binding.previous.setOnClickListener(view ->  { loadpdf(binding.pdfView.getCurrentPage()-1);});
        binding.forward.setOnClickListener(view ->  { loadpdf(binding.pdfView.getCurrentPage()+1) ;});
        binding.mode.setOnClickListener(view ->  {
        SharedPreferences.Editor editor=getSharedPreferences("mode", MODE_PRIVATE).edit();
        editor.putBoolean("mode",preference.getBoolean("mode",false));
        editor.apply();
        binding.mode.setRotation(binding.mode.getRotation()+90);
        loadpdf(binding.pdfView.getCurrentPage());

        });

        binding.last.setOnClickListener (view -> { loadpdf(binding.pdfView.getPageCount()) ;});

        binding.pageSelection.setOnClickListener(view -> {
                    NumberPicker numberPicker = new NumberPicker(this);
                    //   changeDividerColor(numberPicker, Color.parseColor("#20000000"));
                    numberPicker.setMaxValue(binding.pdfView.getPageCount());
                    numberPicker.setMinValue(0);
                    numberPicker.setValue(binding.pdfView.getCurrentPage());


                    AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                    builder.setView(numberPicker);
                    builder.setTitle("Select Page");
                    builder.setPositiveButton("OK", (dialogInterface, i) -> loadpdf(numberPicker.getValue()));


                    builder.create();
                    builder.show();
                });




        loadpdf(0);

    }

private void loadpdf(int i) {
        binding.progress.setVisibility(View.VISIBLE);
        if (preference != null) {
        File file=new File(getIntent().getStringExtra("path"));
        Log.i("123321", "loadpdf: file exits is ${file.exists()}");
        binding.pdfView.fromFile(file).enableSwipe(true)
        .defaultPage(i)
        .onLoad(nbPages -> binding.progress.setVisibility(View.GONE))

        .swipeHorizontal(preference.getBoolean("mode",false))
            .load();
        }

        }



        }