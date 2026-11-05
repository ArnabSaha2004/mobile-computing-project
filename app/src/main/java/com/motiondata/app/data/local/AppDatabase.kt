package com.motiondata.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.motiondata.app.data.local.dao.SensorSampleDao
import com.motiondata.app.data.local.dao.SessionDao
import com.motiondata.app.data.local.entity.RecordingSessionEntity
import com.motiondata.app.data.local.entity.SensorSampleEntity

@Database(
    entities = [
        RecordingSessionEntity::class,
        SensorSampleEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao
    abstract fun sensorSampleDao(): SensorSampleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "motion_data.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
