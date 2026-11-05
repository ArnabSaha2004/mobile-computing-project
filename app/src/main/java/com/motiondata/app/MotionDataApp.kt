package com.motiondata.app

import android.app.Application
import com.motiondata.app.data.local.AppDatabase

class MotionDataApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Pre-initialize database on application startup
        AppDatabase.getDatabase(this)
    }
}
