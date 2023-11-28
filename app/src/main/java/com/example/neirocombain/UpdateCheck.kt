package com.example.neirocombain

import android.content.ComponentCallbacks
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import java.time.DayOfWeek
import java.time.LocalDate


class UpdateCheck {
    fun check(context: Context, _was_recently_seen: Boolean) {
        val now = LocalDate.now()
        var was_recently_seen = _was_recently_seen
        if (now.dayOfWeek == DayOfWeek.MONDAY || now.dayOfWeek == DayOfWeek.TUESDAY) {
            if (!was_recently_seen) {
                val intent1 = Intent(context, UpdateActivityActivity::class.java)
                startActivity(context, intent1, null)
                was_recently_seen = true

            }


        }
    }
}