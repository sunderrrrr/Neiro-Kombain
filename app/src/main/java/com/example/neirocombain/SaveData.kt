package com.example.neirocombain

import android.content.SharedPreferences
import com.google.gson.Gson

class SaveData() {
    val gson = Gson()
    fun Save(pref: SharedPreferences, res: Int, bool: Boolean, array: ArrayList<MessageRVModal>){
        val editor = pref?.edit()
        val json = gson.toJson(array)
        editor?.putString("ui_msg", json)
        editor?.putInt("attempts", res)
        editor?.putBoolean("wrs", bool)
        editor?.apply()
    }
    fun saveArray(){

    }

}