package com.gameturbo.ai

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.gameturbo.ai.performance.SystemTelemetryReader
import com.gameturbo.ai.security.SecurityValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GameTurboSuiteTest {

    @Test
    fun `security validator allows sandboxed cache file and blocks traversal`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val validCacheFile = File(context.cacheDir, "sample.tmp").apply {
            writeText("ok")
        }
        assertTrue(SecurityValidator.isAppOwnedDeletableFile(context, validCacheFile))

        val traversalFile = File(context.cacheDir, "../shared_prefs/outside.xml")
        assertFalse(SecurityValidator.isAppOwnedDeletableFile(context, traversalFile))

        val externalFile = File("/data/local/tmp/system_file")
        assertFalse(SecurityValidator.isAppOwnedDeletableFile(context, externalFile))

        validCacheFile.delete()
    }

    @Test
    fun `security validator sanitizes control characters and enforces max length`() {
        val cleaned = SecurityValidator.sanitizeLabel("  Gaming\n\tPro\u0000  ", maxLength = 12)
        assertEquals("GamingPro", cleaned)
    }

    @Test
    fun `telemetry reader captures real non-negative device metrics`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val reader = SystemTelemetryReader(context)
        val snapshot = reader.captureSnapshot()

        assertTrue(snapshot.ram.totalMb > 0L)
        assertTrue(snapshot.cpu.coreCount >= 1)
        assertTrue(snapshot.storage.totalBytes > 0L)
        assertTrue(snapshot.display.densityDpi > 0)
    }
}
