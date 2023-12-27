package com.example.neirocombain

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

@Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
class Settings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val back_btn = findViewById<ImageView>(R.id.back_to_main)
        val seekbar = findViewById<SeekBar>(R.id.temp_bar)
        var final_temp = 0
        lateinit var selectedLang: String
        val langTV = findViewById<AutoCompleteTextView>(R.id.SettingsLangTV)
        val languages = resources.getStringArray(R.array.lang_array)
        val arrayAdapter = ArrayAdapter(/* context = */ this, /* resource = */ R.layout.dropdown_item, /* objects = */ languages)
        langTV.setAdapter(arrayAdapter)
        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                Toast.makeText(applicationContext, "seekbar progress: $progress", Toast.LENGTH_SHORT).show()
                final_temp = (progress * 0.1).toInt()

            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                println("")
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                println("")
            }

        })
        back_btn.setOnClickListener{
            val intent1 = Intent(this, MainActivity::class.java)

            ContextCompat.startActivity(this, intent1, null).apply {
                intent1.putExtra("temp", final_temp)
            }

        }

        langTV.onItemClickListener= AdapterView.OnItemClickListener { adapterView, view, i, l ->
            selectedLang = adapterView.getItemAtPosition(i).toString()

        }
}
}