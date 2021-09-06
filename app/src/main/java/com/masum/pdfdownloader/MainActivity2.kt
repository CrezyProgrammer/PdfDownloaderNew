package com.masum.pdfdownloader

import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import com.masum.pdfdownloader.databinding.ActivityMainBinding
import com.masum.pdfdownloader.databinding.ActivityPdfBinding
import java.io.File
import java.lang.reflect.Field


class MainActivity2 : AppCompatActivity() {
    var preference:SharedPreferences?=null
    private final var TAG ="123321"
    private lateinit var binding: ActivityPdfBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preference=getSharedPreferences("mode", MODE_PRIVATE)
        binding.first.setOnClickListener {  loadpdf(0)}
        binding.previous.setOnClickListener { loadpdf(binding.pdfView.currentPage-1)}
        binding.forward.setOnClickListener { loadpdf(binding.pdfView.currentPage+1) }
        binding.mode.setOnClickListener {
            val editor=getSharedPreferences("mode", MODE_PRIVATE).edit()
            editor.putBoolean("mode",!preference!!.getBoolean("mode",false))
            editor.apply()
            binding.mode.rotation=binding.mode.rotation+90
            loadpdf(binding.pdfView.currentPage)

        }

binding.last.setOnClickListener { loadpdf(binding.pdfView.pageCount) }

        binding.pageSelection.setOnClickListener {
            val numberPicker = NumberPicker(this)
            changeDividerColor(numberPicker, Color.parseColor("#20000000"));
            numberPicker.maxValue = binding.pdfView.pageCount
            numberPicker.minValue = 0
            numberPicker.value=binding.pdfView.currentPage


            val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
            builder.setView(numberPicker)
            builder.setTitle("Select Page")
            builder.setPositiveButton("OK"
            ) { dialog, which ->
                run {
                   loadpdf(numberPicker.value)

                }

            }
            builder.setNegativeButton("CANCEL",
                DialogInterface.OnClickListener { dialog, which -> })
            builder.create()
            builder.show()
        }


 loadpdf(0)
    }

    private fun loadpdf(i: Int) {
    binding.progress.visibility=View.VISIBLE
        if (preference != null) {
            val file=File(intent.getStringExtra("path"))
            Log.i("123321", "loadpdf: file exits is ${file.exists()}")
            binding.pdfView.fromFile(file).enableSwipe(true).onPageChange { page, pageCount ->
                Log.i(
                    "123321",
                    "page:$page count:$pageCount"
                )

            }.defaultPage(i)
                .onLoad {
                    binding.progress.visibility=View.GONE

                }
                .onError {
                    Log.i("123321", "loadpdf: ")
                }
                .swipeHorizontal(preference!!.getBoolean("mode",false)).load()
        }

    }

    private fun changeDividerColor(picker: NumberPicker, color: Int) {
        val pickerFields = NumberPicker::class.java.declaredFields
        for (pf in pickerFields) {
            if (pf.name == "mSelectionDivider") {
                pf.isAccessible = true
                try {
                    val colorDrawable = ColorDrawable(color)
                    pf[picker] = colorDrawable
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                } catch (e: Resources.NotFoundException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
                break
            }
        }
    }


}