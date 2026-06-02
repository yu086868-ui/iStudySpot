package com.example.scylier.istudyspot

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.scylier.istudyspot.ui.theme.ThemeMode
import com.example.scylier.istudyspot.ui.theme.ThemeState
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
class ThemeStateTest {

    private lateinit var configManager: ConfigManager
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        configManager = ConfigManager.getInstance(context)
        configManager.clearAll()
        ThemeState.themeMode = ThemeMode.SYSTEM
    }

    @After
    fun teardown() {
        configManager.clearAll()
        ThemeState.themeMode = ThemeMode.SYSTEM
    }

    @Test
    fun testDefaultThemeMode() {
        assertEquals(ThemeMode.SYSTEM, ThemeState.themeMode)
    }

    @Test
    fun testToggle_lightToDark() {
        ThemeState.themeMode = ThemeMode.LIGHT
        ThemeState.toggle()
        assertEquals(ThemeMode.DARK, ThemeState.themeMode)
    }

    @Test
    fun testToggle_darkToSystem() {
        ThemeState.themeMode = ThemeMode.DARK
        ThemeState.toggle()
        assertEquals(ThemeMode.SYSTEM, ThemeState.themeMode)
    }

    @Test
    fun testToggle_systemToLight() {
        ThemeState.themeMode = ThemeMode.SYSTEM
        ThemeState.toggle()
        assertEquals(ThemeMode.LIGHT, ThemeState.themeMode)
    }

    @Test
    fun testToggle_fullCycle() {
        assertEquals(ThemeMode.SYSTEM, ThemeState.themeMode)
        ThemeState.toggle()
        assertEquals(ThemeMode.LIGHT, ThemeState.themeMode)
        ThemeState.toggle()
        assertEquals(ThemeMode.DARK, ThemeState.themeMode)
        ThemeState.toggle()
        assertEquals(ThemeMode.SYSTEM, ThemeState.themeMode)
    }

    @Test
    fun testSaveThemeTo_light() {
        ThemeState.themeMode = ThemeMode.LIGHT
        ThemeState.saveThemeTo(configManager)
        assertEquals("LIGHT", configManager.getThemeMode())
    }

    @Test
    fun testSaveThemeTo_dark() {
        ThemeState.themeMode = ThemeMode.DARK
        ThemeState.saveThemeTo(configManager)
        assertEquals("DARK", configManager.getThemeMode())
    }

    @Test
    fun testSaveThemeTo_system() {
        ThemeState.themeMode = ThemeMode.SYSTEM
        ThemeState.saveThemeTo(configManager)
        assertEquals("SYSTEM", configManager.getThemeMode())
    }

    @Test
    fun testLoadSavedTheme_dark() {
        configManager.saveThemeMode("DARK")
        ThemeState.loadSavedTheme(configManager)
        assertEquals(ThemeMode.DARK, ThemeState.themeMode)
    }

    @Test
    fun testLoadSavedTheme_light() {
        configManager.saveThemeMode("LIGHT")
        ThemeState.loadSavedTheme(configManager)
        assertEquals(ThemeMode.LIGHT, ThemeState.themeMode)
    }

    @Test
    fun testLoadSavedTheme_system() {
        configManager.saveThemeMode("SYSTEM")
        ThemeState.loadSavedTheme(configManager)
        assertEquals(ThemeMode.SYSTEM, ThemeState.themeMode)
    }

    @Test
    fun testLoadSavedTheme_null_keepsDefault() {
        ThemeState.themeMode = ThemeMode.SYSTEM
        ThemeState.loadSavedTheme(configManager)
        assertEquals(ThemeMode.SYSTEM, ThemeState.themeMode)
    }

    @Test
    fun testLoadSavedTheme_unknownValue_defaultsToSystem() {
        configManager.saveThemeMode("UNKNOWN_MODE")
        ThemeState.loadSavedTheme(configManager)
        assertEquals(ThemeMode.SYSTEM, ThemeState.themeMode)
    }

    @Test
    fun testSaveAndLoad_roundTrip() {
        ThemeState.themeMode = ThemeMode.DARK
        ThemeState.saveThemeTo(configManager)

        ThemeState.themeMode = ThemeMode.SYSTEM
        assertEquals(ThemeMode.SYSTEM, ThemeState.themeMode)

        ThemeState.loadSavedTheme(configManager)
        assertEquals(ThemeMode.DARK, ThemeState.themeMode)
    }

    @Test
    fun testThemeMode_enumValues() {
        val values = ThemeMode.values()
        assertEquals(3, values.size)
        assertTrue(values.contains(ThemeMode.LIGHT))
        assertTrue(values.contains(ThemeMode.DARK))
        assertTrue(values.contains(ThemeMode.SYSTEM))
    }

    @Test
    fun testThemeMode_valueOf() {
        assertEquals(ThemeMode.LIGHT, ThemeMode.valueOf("LIGHT"))
        assertEquals(ThemeMode.DARK, ThemeMode.valueOf("DARK"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.valueOf("SYSTEM"))
    }
}
