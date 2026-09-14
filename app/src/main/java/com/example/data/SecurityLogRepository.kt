package com.example.data

import kotlinx.coroutines.flow.Flow

class SecurityLogRepository(private val dao: SecurityLogDao) {
    val logs: Flow<List<SecurityLogEntity>> = dao.getAllLogs()

    suspend fun addLog(actionType: String, title: String, details: String, isProtected: Boolean) {
        dao.insertLog(
            SecurityLogEntity(
                actionType = actionType,
                title = title,
                details = details,
                isProtected = isProtected
            )
        )
    }

    suspend fun clearAll() {
        dao.clearLogs()
    }
}
