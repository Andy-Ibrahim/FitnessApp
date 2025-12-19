package com.example.classpass

import android.app.Application
import com.example.classpass.data.database.AppDatabase
import com.example.classpass.data.model.User
import com.example.classpass.data.repository.UserRepository
import com.example.classpass.data.repository.ChatMessageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClassPassApplication : Application() {
    
    val database by lazy { AppDatabase.getDatabase(this) }
    val userRepository by lazy { UserRepository(database.userDao()) }
    val chatSessionRepository by lazy { 
        com.example.classpass.data.repository.ChatSessionRepository(
            database.chatSessionDao(),
            database.chatMessageDao()
        )
    }
    val chatRepository by lazy { ChatMessageRepository(database.chatMessageDao()) }
    // Template-based workout repository (NEW SYSTEM)
    val workoutRepository by lazy {
        com.example.classpass.data.repository.WorkoutRepository(
            database.workoutTemplateDao(),
            database.workoutDayDao(),
            database.programScheduleDao(),
            database.workoutHistoryDao(),
            database.restDayLogDao()
        )
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Create default user on first launch
        val isFirstLaunch = getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getBoolean("is_first_launch", true)
        
        if (isFirstLaunch) {
            CoroutineScope(Dispatchers.IO).launch {
                val database = AppDatabase.getDatabase(applicationContext)
                val userDao = database.userDao()
                
                // Create default user
                val defaultUser = User(
                    userId = 1,
                    name = "Andy",
                    email = "andy@voicefitness.com",
                    phoneNumber = null,
                    dateOfBirth = null,
                    height = null,
                    weight = null,
                    fitnessGoal = null,
                    preferredActivities = null
                )
                userDao.insertUser(defaultUser)
                
                // Mark as not first launch
                getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("is_first_launch", false)
                    .apply()
            }
        }
    }
}

