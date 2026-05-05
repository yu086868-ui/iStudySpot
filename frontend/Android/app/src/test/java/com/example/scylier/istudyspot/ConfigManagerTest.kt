package com.example.scylier.istudyspot

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.scylier.istudyspot.utils.ConfigManager
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class ConfigManagerTest {

    private lateinit var configManager: ConfigManager
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        configManager = ConfigManager.getInstance(context)
        configManager.clearAll()
    }

    @After
    fun teardown() {
        configManager.clearAll()
    }

    @Test
    fun testGetInstance_returnsSameInstance() {
        val instance1 = ConfigManager.getInstance(context)
        val instance2 = ConfigManager.getInstance(context)

        assertSame(instance1, instance2)
    }

    @Test
    fun testSaveToken_andGetToken() {
        configManager.saveToken("test_token_123")

        assertEquals("test_token_123", configManager.getToken())
    }

    @Test
    fun testGetToken_whenNotSet_returnsNull() {
        assertNull(configManager.getToken())
    }

    @Test
    fun testRemoveToken() {
        configManager.saveToken("test_token")
        configManager.removeToken()

        assertNull(configManager.getToken())
    }

    @Test
    fun testSaveUserId_andGetUserId() {
        configManager.saveUserId("user_123")

        assertEquals("user_123", configManager.getUserId())
    }

    @Test
    fun testGetUserId_whenNotSet_returnsNull() {
        assertNull(configManager.getUserId())
    }

    @Test
    fun testSaveUsername_andGetUsername() {
        configManager.saveUsername("testuser")

        assertEquals("testuser", configManager.getUsername())
    }

    @Test
    fun testGetUsername_whenNotSet_returnsNull() {
        assertNull(configManager.getUsername())
    }

    @Test
    fun testSaveNickname_andGetNickname() {
        configManager.saveNickname("测试昵称")

        assertEquals("测试昵称", configManager.getNickname())
    }

    @Test
    fun testGetNickname_whenNotSet_returnsNull() {
        assertNull(configManager.getNickname())
    }

    @Test
    fun testClearAll_removesAllData() {
        configManager.saveToken("token")
        configManager.saveUserId("userId")
        configManager.saveUsername("username")
        configManager.saveNickname("nickname")

        configManager.clearAll()

        assertNull(configManager.getToken())
        assertNull(configManager.getUserId())
        assertNull(configManager.getUsername())
        assertNull(configManager.getNickname())
    }

    @Test
    fun testSaveToken_overwritesExisting() {
        configManager.saveToken("first_token")
        configManager.saveToken("second_token")

        assertEquals("second_token", configManager.getToken())
    }

    @Test
    fun testSaveUserId_overwritesExisting() {
        configManager.saveUserId("first_id")
        configManager.saveUserId("second_id")

        assertEquals("second_id", configManager.getUserId())
    }

    @Test
    fun testSaveUsername_overwritesExisting() {
        configManager.saveUsername("first_user")
        configManager.saveUsername("second_user")

        assertEquals("second_user", configManager.getUsername())
    }

    @Test
    fun testSaveNickname_overwritesExisting() {
        configManager.saveNickname("first_nick")
        configManager.saveNickname("second_nick")

        assertEquals("second_nick", configManager.getNickname())
    }

    @Test
    fun testMultipleOperations() {
        configManager.saveToken("token123")
        configManager.saveUserId("user456")
        configManager.saveUsername("testuser")
        configManager.saveNickname("测试用户")

        assertEquals("token123", configManager.getToken())
        assertEquals("user456", configManager.getUserId())
        assertEquals("testuser", configManager.getUsername())
        assertEquals("测试用户", configManager.getNickname())
    }
}
