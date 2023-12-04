package com.example.neirocombain

import android.content.SharedPreferences

class SaveData() {
    fun Save(pref: SharedPreferences, res: Int, bool: Boolean){
        val editor = pref?.edit()
        editor?.putInt("attempts", res)
        editor?.putBoolean("wrs", bool)
        editor?.apply()
    }

}