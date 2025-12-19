package com.example.classpass.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.classpass.data.model.User

@Dao
interface UserDao {
    
    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUser(): LiveData<User?>
    
    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: Long): User?
    
    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUserSync(): User?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long
    
    @Update
    suspend fun updateUser(user: User)
    
    @Delete
    suspend fun deleteUser(user: User)
    
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}

