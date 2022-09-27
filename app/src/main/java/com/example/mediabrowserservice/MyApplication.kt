package com.example.mediabrowserservice

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.mediabrowserservice.utils.Constant

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        onCreateChannelNotification()
    }

    private fun onCreateChannelNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                Constant.CHANNEL_ID,
                Constant.CHANNEL_MEDIA,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
