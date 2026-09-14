package com.example.ui

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SecurityLogEntity
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.ShieldAmber
import com.example.ui.theme.ShieldCyan
import com.example.ui.theme.ShieldElectricBlue
import com.example.ui.theme.ShieldNavy
import com.example.ui.theme.ShieldSecureGreen
import com.example.ui.theme.ShieldWarningRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: PrivacyViewModel,
    uiState: PrivacyUiState,
    onLaunchDeviceAdmin: (Intent) -> Unit,
    onLaunchSettings: (Intent) -> Unit
) {
    val context = LocalContext.current

    // Force Hebrew RTL layout
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "לוגו מגן אבטחה",
                                tint = ShieldCyan,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = "חוסם מצלמה ומיקרופון",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "הגנת חומרה ופרטיות במכשיר",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CyberBackground
                    ),
                    actions = {
                        IconButton(
                            onClick = { viewModel.refreshState() },
                            modifier = Modifier.testTag("refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "רענן מצב",
                                tint = TextSecondary
                            )
                        }
                    }
                )
            },
            containerColor = CyberBackground
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 36.dp, top = 8.dp)
            ) {
                // 1. Hero Shield Banner
                item {
                    MasterShieldHero(
                        isCamBlocked = uiState.isCameraBlocked,
                        isMicBlocked = uiState.isMicBlocked,
                        onToggleMaster = {
                            val bothBlocked = uiState.isCameraBlocked && uiState.isMicBlocked
                            viewModel.onToggleAll(!bothBlocked)
                        }
                    )
                }

                // Device Admin Notice Banner if not active
                if (!uiState.isDeviceAdminActive) {
                    item {
                        DeviceAdminNoticeCard(
                            onActivate = {
                                val intent = viewModel.getPrivacyBlockManager().getDeviceAdminIntent()
                                onLaunchDeviceAdmin(intent)
                            }
                        )
                    }
                }

                // 2. Hardware Blockers Controls
                item {
                    Text(
                        text = "בקרת חסימת חומרה",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                item {
                    BlockerCard(
                        title = "חסימת מצלמה",
                        subtitle = if (uiState.isCameraBlocked)
                            "כל המצלמות במכשיר חסומות ומנוטרלות (Device Policy)"
                        else
                            "המצלמה פתוחה וזמינה לשימוש על ידי אפליקציות",
                        isBlocked = uiState.isCameraBlocked,
                        icon = if (uiState.isCameraBlocked) Icons.Default.VideocamOff else Icons.Default.Videocam,
                        testTag = "camera_block_switch",
                        badgeText = if (uiState.isCameraBlocked) "חסומה ומאובטחת" else "חשופה",
                        badgeColor = if (uiState.isCameraBlocked) ShieldSecureGreen else ShieldWarningRed,
                        onToggle = { viewModel.onToggleCameraBlock() }
                    )
                }

                item {
                    BlockerCard(
                        title = "חסימת מיקרופון",
                        subtitle = if (uiState.isMicBlocked)
                            "ערוץ השמע מושתק ומנוטרל לחלוטין (Audio Mute)"
                        else
                            "המיקרופון פעיל וקולט שמע",
                        isBlocked = uiState.isMicBlocked,
                        icon = if (uiState.isMicBlocked) Icons.Default.MicOff else Icons.Default.Mic,
                        testTag = "mic_block_switch",
                        badgeText = if (uiState.isMicBlocked) "מושתק ומאובטח" else "פעיל",
                        badgeColor = if (uiState.isMicBlocked) ShieldSecureGreen else ShieldWarningRed,
                        onToggle = { viewModel.onToggleMicBlock() }
                    )
                }

                // 3. Real-time Live Testing Sandbox
                item {
                    HardwareTestSection(
                        cameraState = uiState.cameraTestState,
                        micState = uiState.micTestState,
                        micAmplitude = uiState.micLiveAmplitude,
                        onTestCamera = { viewModel.runCameraHardwareTest() },
                        onResetCamera = { viewModel.resetCameraTest() },
                        onTestMic = { viewModel.runMicHardwareTest() },
                        onResetMic = { viewModel.resetMicTest() }
                    )
                }

                // 4. Quick Actions and System Settings
                item {
                    SystemShortcutsSection(
                        isContinuousEnabled = uiState.isContinuousServiceEnabled,
                        onToggleContinuous = { viewModel.toggleContinuousService(it) },
                        onOpenPrivacySettings = {
                            val intent = viewModel.getPrivacyBlockManager().getPrivacySettingsIntent()
                            onLaunchSettings(intent)
                        }
                    )
                }

                // 5. Security Audit Log
                item {
                    SecurityAuditLogView(
                        logs = uiState.securityLogs,
                        onClearLogs = { viewModel.clearAuditLogs() }
                    )
                }
            }
        }

        // Device Admin Guidance Dialog
        if (uiState.showAdminDialog) {
            DeviceAdminDialog(
                onConfirm = {
                    viewModel.dismissAdminDialog()
                    val intent = viewModel.getPrivacyBlockManager().getDeviceAdminIntent()
                    onLaunchDeviceAdmin(intent)
                },
                onDismiss = { viewModel.dismissAdminDialog() }
            )
        }
    }
}

@Composable
fun MasterShieldHero(
    isCamBlocked: Boolean,
    isMicBlocked: Boolean,
    onToggleMaster: () -> Unit
) {
    val bothBlocked = isCamBlocked && isMicBlocked
    val partiallyBlocked = (isCamBlocked || isMicBlocked) && !bothBlocked

    val statusColor by animateColorAsState(
        targetValue = when {
            bothBlocked -> ShieldSecureGreen
            partiallyBlocked -> ShieldAmber
            else -> ShieldWarningRed
        },
        label = "status_color"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberBorder, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Glowing Shield Graphic
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(if (bothBlocked) pulseScale else 1f)
                    .background(
                        color = statusColor.copy(alpha = 0.12f),
                        shape = CircleShape
                    )
                    .border(2.dp, statusColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        bothBlocked -> Icons.Default.Shield
                        partiallyBlocked -> Icons.Default.Lock
                        else -> Icons.Default.LockOpen
                    },
                    contentDescription = "סמל מצב הגנה",
                    tint = statusColor,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when {
                    bothBlocked -> "הגנה מירבית פעילה 🔒"
                    partiallyBlocked -> "הגנה חלקית מופעלת ⚠️"
                    else -> "החומרה חשופה לחלוטין 🔓"
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = when {
                    bothBlocked -> "המצלמה והמיקרופון חסומים במכשיר. אפליקציות אינן יכולות לראות או להאזין."
                    partiallyBlocked -> "אחד מחיישני הפרטיות אינו מוגן. מומלץ לחסום את שניהם."
                    else -> "מצלמה ומיקרופון פתוחים כעת. אפליקציות עלולות לגשת אליהם."
                },
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onToggleMaster,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (bothBlocked) CyberSurfaceVariant else ShieldCyan,
                    contentColor = if (bothBlocked) TextPrimary else Color(0xFF00382F)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("master_toggle_button")
            ) {
                Icon(
                    imageVector = if (bothBlocked) Icons.Default.LockOpen else Icons.Default.Shield,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (bothBlocked) "שחרר את כל החסימות" else "הפעל הגנה מירבית מיידית",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DeviceAdminNoticeCard(onActivate: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF261908)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ShieldAmber.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = "התראת מנהל מכשיר",
                tint = ShieldAmber,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "נדרשת הרשאת מנהל מכשיר",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD580)
                )
                Text(
                    text = "כדי לחסום את המצלמה ברמת החומרה, יש לאשר הרשאת מנהל באנדרואיד.",
                    fontSize = 12.sp,
                    color = Color(0xFFE2C48F),
                    lineHeight = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onActivate,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ShieldAmber),
                modifier = Modifier.testTag("activate_admin_button")
            ) {
                Text(
                    text = "הפעל",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun BlockerCard(
    title: String,
    subtitle: String,
    isBlocked: Boolean,
    icon: ImageVector,
    testTag: String,
    badgeText: String,
    badgeColor: Color,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isBlocked) ShieldCyan.copy(alpha = 0.4f) else CyberBorder, RoundedCornerShape(18.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (isBlocked) ShieldCyan.copy(alpha = 0.15f) else CyberSurfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isBlocked) ShieldCyan else TextSecondary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Surface(
                        color = badgeColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Switch(
                checked = isBlocked,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ShieldCyan,
                    checkedTrackColor = Color(0xFF004439),
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = CyberSurfaceVariant
                ),
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}

@Composable
fun HardwareTestSection(
    cameraState: HardwareTestState,
    micState: HardwareTestState,
    micAmplitude: Int,
    onTestCamera: () -> Unit,
    onResetCamera: () -> Unit,
    onTestMic: () -> Unit,
    onResetMic: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberBorder, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = ShieldElectricBlue,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "בדיקת חומרה בזמן אמת",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "בדוק האם החסימה מצליחה למנוע גישה לחומרה של המצלמה והמיקרופון בפועל.",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Camera Test Row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberSurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "בדיקת גישה למצלמה",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "ניסיון פתיחת חיישן Camera2",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    when (cameraState) {
                        is HardwareTestState.Running -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = ShieldCyan,
                                strokeWidth = 2.dp
                            )
                        }
                        else -> {
                            FilledTonalButton(
                                onClick = onTestCamera,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = CyberBorder,
                                    contentColor = TextPrimary
                                ),
                                modifier = Modifier.testTag("test_camera_button")
                            ) {
                                Text("בדוק מצלמה", fontSize = 12.sp)
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = cameraState is HardwareTestState.Completed) {
                    if (cameraState is HardwareTestState.Completed) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .background(
                                    color = if (cameraState.isBlocked) Color(0x2210B981) else Color(0x22F43F5E),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Text(
                                text = if (cameraState.isBlocked) "🛡️ תוצאה: המצלמה חסומה בהצלחה!" else "⚠️ תוצאה: המצלמה פתוחה!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (cameraState.isBlocked) ShieldSecureGreen else ShieldWarningRed
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = cameraState.message,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mic Test Row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberSurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "בדיקת אות מיקרופון",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "מדידת דציבלים וקליטת שמע",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    when (micState) {
                        is HardwareTestState.Running -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = ShieldCyan,
                                strokeWidth = 2.dp
                            )
                        }
                        else -> {
                            FilledTonalButton(
                                onClick = onTestMic,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = CyberBorder,
                                    contentColor = TextPrimary
                                ),
                                modifier = Modifier.testTag("test_mic_button")
                            ) {
                                Text("בדוק מיקרופון", fontSize = 12.sp)
                            }
                        }
                    }
                }

                if (micState is HardwareTestState.Running) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "דוגם שמע בזמן אמת... רמת אות: $micAmplitude",
                        fontSize = 11.sp,
                        color = ShieldCyan
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val progress = (micAmplitude.toFloat() / 5000f).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (micAmplitude < 200) ShieldSecureGreen else ShieldWarningRed,
                        trackColor = CyberBorder
                    )
                }

                AnimatedVisibility(visible = micState is HardwareTestState.Completed) {
                    if (micState is HardwareTestState.Completed) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .background(
                                    color = if (micState.isBlocked) Color(0x2210B981) else Color(0x22F43F5E),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Text(
                                text = if (micState.isBlocked) "🛡️ תוצאה: המיקרופון מושתק ומנוטרל!" else "⚠️ תוצאה: המיקרופון קולט שמע!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (micState.isBlocked) ShieldSecureGreen else ShieldWarningRed
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = micState.message,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SystemShortcutsSection(
    isContinuousEnabled: Boolean,
    onToggleContinuous: (Boolean) -> Unit,
    onOpenPrivacySettings: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberBorder, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = "אפשרויות הגנה נוספות",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Continuous foreground service toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "שירות הגנה רציף ברקע",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "מציג סטטוס במגירת ההתראות ומבטיח שהמיקרופון נשאר מושתק",
                        fontSize = 11.sp,
                        color = TextMuted,
                        lineHeight = 15.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = isContinuousEnabled,
                    onCheckedChange = onToggleContinuous,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ShieldCyan,
                        checkedTrackColor = Color(0xFF004439)
                    ),
                    modifier = Modifier.testTag("continuous_service_switch")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Android Privacy Dashboard Shortcut
            OutlinedButton(
                onClick = onOpenPrivacySettings,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("android_privacy_dashboard_button"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextPrimary
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = ShieldCyan
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "פתח לוח בקרת פרטיות של Android",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SecurityAuditLogView(
    logs: List<SecurityLogEntity>,
    onClearLogs: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberBorder, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "יומן אירועי אבטחה",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (logs.isNotEmpty()) {
                        Surface(
                            color = CyberSurfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "${logs.size}",
                                fontSize = 11.sp,
                                color = ShieldCyan,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (logs.isNotEmpty()) {
                    IconButton(
                        onClick = onClearLogs,
                        modifier = Modifier.size(32.dp).testTag("clear_logs_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "נקה יומן",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "אין אירועים עדיין. כל פעולת חסימה או בדיקה תתועד כאן.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    logs.take(5).forEach { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CyberSurfaceVariant, RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = if (log.isProtected) ShieldSecureGreen else ShieldWarningRed,
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = log.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = log.details,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = dateFormat.format(Date(log.timestamp)),
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceAdminDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = ShieldCyan
                )
                Text(
                    text = "הפעלת מנהל מכשיר",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "באנדרואיד, חסימת מצלמה ברמת המערכת מתבצעת באמצעות מדיניות אבטחה רשמית של 'מנהל מכשיר' (Device Administrator).",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
                Text(
                    text = "• חוסם את כל המצלמות (קדמית ואחורית) במכשיר.\n• שום אפליקציה לא תוכל להפעיל צילום.\n• ניתן לבטל את ההרשאה בכל עת בקלות.",
                    fontSize = 12.sp,
                    color = TextPrimary,
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ShieldCyan),
                modifier = Modifier.testTag("dialog_confirm_admin")
            ) {
                Text("המשך להפעלה", color = Color(0xFF00382F), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ביטול", color = TextSecondary)
            }
        },
        containerColor = CyberSurface,
        shape = RoundedCornerShape(20.dp)
    )
}
