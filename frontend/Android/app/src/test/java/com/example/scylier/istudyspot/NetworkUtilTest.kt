package com.example.scylier.istudyspot

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.scylier.istudyspot.utils.NetworkUtil
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class NetworkUtilTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testIsNetworkAvailable_returnsBoolean() {
        val result = NetworkUtil.isNetworkAvailable(context)
        assertTrue(result is Boolean)
    }

    @Test
    fun testIsWifiConnected_returnsBoolean() {
        val result = NetworkUtil.isWifiConnected(context)
        assertTrue(result is Boolean)
    }

    @Test
    fun testIsMobileConnected_returnsBoolean() {
        val result = NetworkUtil.isMobileConnected(context)
        assertTrue(result is Boolean)
    }

    @Test
    fun testIsNetworkAvailable_noException() {
        try {
            NetworkUtil.isNetworkAvailable(context)
        } catch (e: Exception) {
            fail("Should not throw exception: ${e.message}")
        }
    }

    @Test
    fun testIsWifiConnected_noException() {
        try {
            NetworkUtil.isWifiConnected(context)
        } catch (e: Exception) {
            fail("Should not throw exception: ${e.message}")
        }
    }

    @Test
    fun testIsMobileConnected_noException() {
        try {
            NetworkUtil.isMobileConnected(context)
        } catch (e: Exception) {
            fail("Should not throw exception: ${e.message}")
        }
    }
}
