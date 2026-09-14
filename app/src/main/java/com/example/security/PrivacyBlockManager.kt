package com.example.security

import android.Manifest
import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.receiver.DeviceAdminBlockReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PrivacyBlockManager(private val context: Context) {

    private val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val adminComponent = ComponentName(context, DeviceAdminBlockReceiver::class.java)

    fun isDeviceAdminActive(): Boolean {
        return dpm.isAdminActive(adminComponent)
    }

    fun getDeviceAdminIntent(): Intent {
        return Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "הפעלת מנהל מכשיר מאפשרת לאפליקציה לחסום את כל מצלמות המכשיר ברמת החומרה ומערכת ההפעלה, כך שאף אפליקציה לא תוכל לצלם."
            )
        }
    }

    fun isCameraBlocked(): Boolean {
        return if (isDeviceAdminActive()) {
            try {
                dpm.getCameraDisabled(adminComponent)
            } catch (_: Exception) {
                false
            }
        } else {
            false
        }
    }

    fun setCameraBlocked(block: Boolean): Boolean {
        if (!isDeviceAdminActive()) {
            return false
        }
        return try {
            dpm.setCameraDisabled(adminComponent, block)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun isMicBlocked(): Boolean {
        return audioManager.isMicrophoneMute
    }

    fun setMicBlocked(block: Boolean) {
        try {
            audioManager.isMicrophoneMute = block
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getPrivacySettingsIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_PRIVACY_SETTINGS)
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS)
        }
    }

    fun getApplicationDetailsIntent(): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", context.packageName, null)
        }
    }

    /**
     * Attempts to open the first available camera to test if it's blocked by Device Admin or OS.
     */
    @SuppressLint("MissingPermission")
    suspend fun testCameraAccess(): Pair<Boolean, String> = withContext(Dispatchers.Default) {
        if (isCameraBlocked()) {
            return@withContext Pair(true, "המצלמה מנוטרלת לחלוטין ברמת המערכת (Device Policy). שום אפליקציה אינה יכולה להפעיל אותה.")
        }

        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasCameraPermission) {
            return@withContext Pair(
                false,
                "נדרשת הרשאת מצלמה לצורך ביצוע בדיקת החומרה באפליקציה."
            )
        }

        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            ?: return@withContext Pair(false, "מנהל המצלמות אינו זמין במכשיר זה.")

        val cameraIds = try {
            cameraManager.cameraIdList
        } catch (e: Exception) {
            return@withContext Pair(true, "הגישה למצלמות חסומה: ${e.localizedMessage}")
        }

        if (cameraIds.isEmpty()) {
            return@withContext Pair(true, "לא נמצאו מצלמות זמינות (ייתכן שנחסמו על ידי המערכת).")
        }

        var isBlocked = false
        var message = ""
        val targetCameraId = cameraIds[0]

        val mainHandler = Handler(Looper.getMainLooper())
        val lock = Object()
        var completed = false

        try {
            cameraManager.openCamera(
                targetCameraId,
                object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice) {
                        isBlocked = false
                        message = "המצלמה נפתחה בהצלחה - המצלמה אינה חסומה כעת!"
                        camera.close()
                        synchronized(lock) {
                            completed = true
                            lock.notifyAll()
                        }
                    }

                    override fun onDisconnected(camera: CameraDevice) {
                        camera.close()
                        isBlocked = true
                        message = "המצלמה נותקה על ידי מערכת ההפעלה."
                        synchronized(lock) {
                            completed = true
                            lock.notifyAll()
                        }
                    }

                    override fun onError(camera: CameraDevice, error: Int) {
                        camera.close()
                        isBlocked = true
                        message = when (error) {
                            ERROR_CAMERA_DISABLED -> "המצלמה חסומה על ידי מדיניות האבטחה (CAMERA_DISABLED)!"
                            ERROR_CAMERA_IN_USE -> "המצלמה בשימוש או חסומה בלעדית."
                            else -> "הגישה למצלמה נכשלה (קוד $error) - חסימה פעילה."
                        }
                        synchronized(lock) {
                            completed = true
                            lock.notifyAll()
                        }
                    }
                },
                mainHandler
            )

            synchronized(lock) {
                if (!completed) {
                    lock.wait(2500)
                }
            }
        } catch (e: CameraAccessException) {
            isBlocked = true
            message = if (e.reason == CameraAccessException.CAMERA_DISABLED) {
                "המצלמה חסומה ומנוטרלת ברמת המכשיר (CAMERA_DISABLED)!"
            } else {
                "שגיאת גישה למצלמה: ${e.localizedMessage}"
            }
        } catch (e: SecurityException) {
            isBlocked = true
            message = "חסימת אבטחה פעילה: אין הרשאת גישה למצלמה."
        } catch (e: Exception) {
            isBlocked = true
            message = "המצלמה לא ניתנת לגישה: ${e.localizedMessage}"
        }

        Pair(isBlocked, if (message.isEmpty()) "בדיקת המצלמה הושלמה." else message)
    }

    /**
     * Tests microphone input to verify mute/blocking status.
     */
    @SuppressLint("MissingPermission")
    suspend fun testMicAccess(
        onAmplitudeSample: (Int) -> Unit
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val isMuted = audioManager.isMicrophoneMute

        val hasAudioPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasAudioPermission) {
            return@withContext Pair(
                isMuted,
                if (isMuted) "המיקרופון מושתק ברמת השמע של המכשיר."
                else "נדרשת הרשאת מיקרופון לבדיקת דגימת שמע."
            )
        }

        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        if (bufferSize <= 0) {
            return@withContext Pair(isMuted, "בדיקת מיקרופון: מושתק = $isMuted")
        }

        var audioRecord: AudioRecord? = null
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                return@withContext Pair(true, "המיקרופון אינו מאותחל או חסום על ידי המערכת.")
            }

            audioRecord.startRecording()
            val buffer = ShortArray(bufferSize / 2)
            var maxAmplitude = 0

            // Sample for ~1 second
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < 1000) {
                val read = audioRecord.read(buffer, 0, buffer.size)
                if (read > 0) {
                    var currentMax = 0
                    for (i in 0 until read) {
                        val abs = Math.abs(buffer[i].toInt())
                        if (abs > currentMax) currentMax = abs
                    }
                    if (currentMax > maxAmplitude) maxAmplitude = currentMax
                    onAmplitudeSample(currentMax)
                }
                Thread.sleep(80)
            }

            audioRecord.stop()

            val isSilent = maxAmplitude < 150 || isMuted
            val msg = if (isSilent) {
                "המיקרופון שקט ומנוטרל לחלוטין (עוצמת דגימה מקסימלית: $maxAmplitude - חסום!)"
            } else {
                "המיקרופון קולט שמע (עוצמת דגימה: $maxAmplitude) - המיקרופון פתוח."
            }

            Pair(isSilent, msg)
        } catch (e: Exception) {
            Pair(true, "הגישה למיקרופון נחסמה: ${e.localizedMessage}")
        } finally {
            try {
                audioRecord?.release()
            } catch (_: Exception) {}
        }
    }
}
