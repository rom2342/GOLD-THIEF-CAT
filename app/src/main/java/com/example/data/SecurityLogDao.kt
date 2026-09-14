package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SecurityLogDao {
    @Query("SELECT * FROM security_logs ORDER BY timestamp DESC LIMIT 50")
    fun getAllLogs(): Flow<List<SecurityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SecurityLogEntity)

    @Query("DELETE FROM security_logs")
    suspend fun clearLogs()
}
