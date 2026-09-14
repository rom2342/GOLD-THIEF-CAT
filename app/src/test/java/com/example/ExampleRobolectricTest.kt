package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SecurityLogEntity
import com.example.security.PrivacyBlockManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("חוסם מצלמה ומיקרופון", appName)
    }

    @Test
    fun `test privacy manager initialization`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = PrivacyBlockManager(context)
        assertNotNull(manager)
        assertNotNull(manager.getDeviceAdminIntent())
    }

    @Test
    fun `test security log entity creation`() {
        val log = SecurityLogEntity(
            id = 1,
            actionType = "CAM_BLOCKED",
            title = "מצלמה נחסמה",
            details = "בדיקה",
            isProtected = true
        )
        assertEquals("CAM_BLOCKED", log.actionType)
        assertEquals(true, log.isProtected)
    }
}
