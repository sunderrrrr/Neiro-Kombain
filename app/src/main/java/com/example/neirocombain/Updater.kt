package com.example.neirocombain

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isImmediateUpdateAllowed

class Updater(var appUpdateManager: AppUpdateManager ) {
    val updateType = AppUpdateType.IMMEDIATE
    fun checlForUpdates(activity: MainActivity) {

        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            val isUpdateAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            val isUpdateSupported = when (updateType) {
                AppUpdateType.IMMEDIATE -> info.isImmediateUpdateAllowed
                else -> false
            }
            if (isUpdateSupported && isUpdateAvailable) {
                println("ПОЯВИЛОСЬ ОБНОВЛЕНИЕ И ОНО ПОДДЕЖИВАЕТСЯ")
                appUpdateManager.startUpdateFlowForResult(info, updateType, activity, 123)
            } else {
                println("ОБНОВЛЕНИЙ НЕТ")
            }
        }
    }


    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 123) {
            if (resultCode != AppCompatActivity.RESULT_OK) {
                println("Something Went Wrong")
            }
        }
    }

    fun resunmeUpdate(activity: MainActivity) {
        if (updateType == AppUpdateType.IMMEDIATE) {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->

                if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    appUpdateManager.startUpdateFlowForResult(info, updateType, activity, 123)
                }
            }
        }
    }
}