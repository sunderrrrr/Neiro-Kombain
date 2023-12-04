package com.example.neirocombain

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class UpdateActivityActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.update_activity)
        val back_btn = findViewById<ImageView>(R.id.back)
        back_btn.setOnClickListener{
            val intent1 = Intent(this, MainActivity::class.java)
            ContextCompat.startActivity(this, intent1, null)

        }
    }
}