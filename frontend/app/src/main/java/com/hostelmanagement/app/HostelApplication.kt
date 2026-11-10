package com.hostelmanagement.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.hostelmanagement.app.core.notification.SmartTrackNotificationHelper
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class HostelApplication : Application(), ImageLoaderFactory {

    @Inject
    lateinit var okHttpClient: OkHttpClient

    override fun onCreate() {
        super.onCreate()
        // Initialize Android notification channels for SmartTrack
        SmartTrackNotificationHelper.createNotificationChannels(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .crossfade(true)
            .build()
    }
}
