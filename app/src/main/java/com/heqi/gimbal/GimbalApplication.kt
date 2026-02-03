package com.heqi.gimbal

import android.app.Application
import timber.log.Timber

class GimbalApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化Timber日志
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        Timber.d("GimbalApplication initialized")
    }
}
