package com.example.neirocombain

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.neirocombain.R

class Settings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val back_btn = findViewById<ImageView>(R.id.back_to_main)
        back_btn.setOnClickListener{
            val intent1 = Intent(this, MainActivity::class.java)
            ContextCompat.startActivity(this, intent1, null)

        }
    }
}