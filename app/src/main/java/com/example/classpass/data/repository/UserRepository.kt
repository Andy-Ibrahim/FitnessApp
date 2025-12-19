package com.example.classpass.data.repository

import androidx.lifecycle.LiveData
import com.example.classpass.data.dao.UserDao
import com.example.classpass.data.model.User

class UserRepository(private val userDao: UserDao) {
    
    val currentUser: LiveData<User?> = userDao.getCurrentUser()
    
    suspend fun getUserById(userId: Long): User? {
        return userDao.getUserById(userId)
    }
    
    suspend fun getCurrentUserSync(): User? {
        return userDao.getCurrentUserSync()
    }
    
    suspend fun insertUser(user: User): Long {
        return userDao.insertUser(user)
    }
    
    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }
    
    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }
    
    suspend fun deleteAllUsers() {
        userDao.deleteAllUsers()
    }
}

