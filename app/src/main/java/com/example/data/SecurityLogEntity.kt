package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "security_logs")
data class SecurityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String,
    val title: String,
    val details: String,
    val isProtected: Boolean
)
