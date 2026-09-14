package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.SecurityLogEntity
import com.example.data.SecurityLogRepository
import com.example.security.PrivacyBlockManager
import com.example.service.PrivacyGuardService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class HardwareTestState {
    object Idle : HardwareTestState()
    object Running : HardwareTestState()
    data class Completed(val isBlocked: Boolean, val message: String) : HardwareTestState()
}

data class PrivacyUiState(
    val isCameraBlocked: Boolean = false,
    val isMicBlocked: Boolean = false,
    val isDeviceAdminActive: Boolean = false,
    val isContinuousServiceEnabled: Boolean = false,
    val cameraTestState: HardwareTestState = HardwareTestState.Idle,
    val micTestState: HardwareTestState = HardwareTestState.Idle,
    val micLiveAmplitude: Int = 0,
    val showAdminDialog: Boolean = false,
    val securityLogs: List<SecurityLogEntity> = emptyList()
)

class PrivacyViewModel(application: Application) : AndroidViewModel(application) {

    private val blockManager = PrivacyBlockManager(application)
    private val repository = SecurityLogRepository(
        AppDatabase.getDatabase(application).securityLogDao()
    )

    private val _uiState = MutableStateFlow(PrivacyUiState())
    val uiState: StateFlow<PrivacyUiState> = _uiState.asStateFlow()

    init {
        refreshState()
        viewModelScope.launch {
            repository.logs.collect { logsList ->
                _uiState.update { it.copy(securityLogs = logsList) }
            }
        }
    }

    fun refreshState() {
        val adminActive = blockManager.isDeviceAdminActive()
        val camBlocked = blockManager.isCameraBlocked()
        val micBlocked = blockManager.isMicBlocked()

        _uiState.update {
            it.copy(
                isDeviceAdminActive = adminActive,
                isCameraBlocked = camBlocked,
                isMicBlocked = micBlocked
            )
        }
    }

    fun onToggleCameraBlock() {
        if (!blockManager.isDeviceAdminActive()) {
            _uiState.update { it.copy(showAdminDialog = true) }
            return
        }

        val target = !_uiState.value.isCameraBlocked
        val success = blockManager.setCameraBlocked(target)
        if (success) {
            val title = if (target) "המצלמה נחסמה" else "המצלמה שוחררה"
            val details = if (target) "חסימת חומרה הופעלה ברמת המערכת" else "הגישה למצלמה הותרה מחדש"
            viewModelScope.launch {
                repository.addLog(
                    actionType = if (target) "CAM_BLOCKED" else "CAM_UNBLOCKED",
                    title = title,
                    details = details,
                    isProtected = target
                )
            }
        }
        refreshState()
    }

    fun onToggleMicBlock() {
        val target = !_uiState.value.isMicBlocked
        blockManager.setMicBlocked(target)

        val title = if (target) "המיקרופון נחסם והושתק" else "המיקרופון שוחרר"
        val details = if (target) "ערוץ השמע הושתק במלואו" else "המיקרופון הוחזר למצב קליטה"
        viewModelScope.launch {
            repository.addLog(
                actionType = if (target) "MIC_BLOCKED" else "MIC_UNBLOCKED",
                title = title,
                details = details,
                isProtected = target
            )
        }
        refreshState()
    }

    fun onToggleAll(block: Boolean) {
        if (block && !blockManager.isDeviceAdminActive()) {
            _uiState.update { it.copy(showAdminDialog = true) }
            // Still block mic immediately
            blockManager.setMicBlocked(true)
            refreshState()
            return
        }

        blockManager.setCameraBlocked(block)
        blockManager.setMicBlocked(block)

        val title = if (block) "הגנה מירבית הופעלה" else "כל החסימות בוטלו"
        val details = if (block) "מצלמה ומיקרופון נחסמו בו זמנית" else "מצלמה ומיקרופון הותרו לשימוש"
        viewModelScope.launch {
            repository.addLog(
                actionType = if (block) "ALL_BLOCKED" else "ALL_UNBLOCKED",
                title = title,
                details = details,
                isProtected = block
            )
        }
        refreshState()
    }

    fun toggleContinuousService(enable: Boolean) {
        _uiState.update { it.copy(isContinuousServiceEnabled = enable) }
        val app = getApplication<Application>()
        if (enable) {
            PrivacyGuardService.startService(app)
        } else {
            PrivacyGuardService.stopService(app)
        }
    }

    fun dismissAdminDialog() {
        _uiState.update { it.copy(showAdminDialog = false) }
    }

    fun runCameraHardwareTest() {
        _uiState.update { it.copy(cameraTestState = HardwareTestState.Running) }
        viewModelScope.launch {
            val (isBlocked, msg) = blockManager.testCameraAccess()
            _uiState.update {
                it.copy(
                    cameraTestState = HardwareTestState.Completed(
                        isBlocked = isBlocked,
                        message = msg
                    )
                )
            }
            repository.addLog(
                actionType = "TEST_CAMERA",
                title = "בדיקת חומרת מצלמה",
                details = msg,
                isProtected = isBlocked
            )
        }
    }

    fun resetCameraTest() {
        _uiState.update { it.copy(cameraTestState = HardwareTestState.Idle) }
    }

    fun runMicHardwareTest() {
        _uiState.update {
            it.copy(
                micTestState = HardwareTestState.Running,
                micLiveAmplitude = 0
            )
        }
        viewModelScope.launch {
            val (isSilent, msg) = blockManager.testMicAccess { amp ->
                _uiState.update { it.copy(micLiveAmplitude = amp) }
            }
            _uiState.update {
                it.copy(
                    micTestState = HardwareTestState.Completed(
                        isBlocked = isSilent,
                        message = msg
                    )
                )
            }
            repository.addLog(
                actionType = "TEST_MIC",
                title = "בדיקת חומרת מיקרופון",
                details = msg,
                isProtected = isSilent
            )
        }
    }

    fun resetMicTest() {
        _uiState.update {
            it.copy(
                micTestState = HardwareTestState.Idle,
                micLiveAmplitude = 0
            )
        }
    }

    fun clearAuditLogs() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun getPrivacyBlockManager(): PrivacyBlockManager = blockManager
}
