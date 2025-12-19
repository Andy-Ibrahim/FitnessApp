package com.example.classpass.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.classpass.data.dao.ChatMessageDao
import com.example.classpass.data.dao.ChatSessionDao
import com.example.classpass.data.dao.UserDao
import com.example.classpass.data.dao.WorkoutHistoryDao
import com.example.classpass.data.dao.WorkoutTemplateDao
import com.example.classpass.data.dao.WorkoutDayDao
import com.example.classpass.data.dao.ProgramScheduleDao
import com.example.classpass.data.dao.RestDayLogDao
import com.example.classpass.data.entity.ChatMessage
import com.example.classpass.data.entity.ChatSession
import com.example.classpass.data.entity.WorkoutHistoryEntity
import com.example.classpass.data.entity.WorkoutTemplateEntity
import com.example.classpass.data.entity.WorkoutDayEntity
import com.example.classpass.data.entity.ProgramScheduleEntity
import com.example.classpass.data.entity.RestDayLogEntity
import com.example.classpass.data.model.User

@Database(
    entities = [
        User::class,
        ChatSession::class,
        ChatMessage::class,
        WorkoutHistoryEntity::class,
        WorkoutTemplateEntity::class,
        WorkoutDayEntity::class,
        ProgramScheduleEntity::class,
        RestDayLogEntity::class
    ],
    version = 23, // Added RestDayLogEntity
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun chatSessionDao(): ChatSessionDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun workoutHistoryDao(): WorkoutHistoryDao
    abstract fun workoutTemplateDao(): WorkoutTemplateDao
    abstract fun workoutDayDao(): WorkoutDayDao
    abstract fun programScheduleDao(): ProgramScheduleDao
    abstract fun restDayLogDao(): RestDayLogDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "classpass_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

