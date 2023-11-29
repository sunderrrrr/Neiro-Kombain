package com.example.neirocombain

import android.content.Context
import android.net.ConnectivityManager
class InternetConnection {
    fun checkConnection(context:Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}