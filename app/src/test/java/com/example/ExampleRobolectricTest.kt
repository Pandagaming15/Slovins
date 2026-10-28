package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.SlovinMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Slovin's Calculator", appName)
    }

    @Test
    fun testSlovinMathIntegration() {
        val calc = SlovinMath.calculate(1000L, 5.0)
        assertNotNull(calc)
        // 1000 / (1 + 1000 * 0.0025) = 1000 / 3.5 = 285.71 -> 286
        assertEquals(286L, calc!!.recommendedSampleSize)
    }
}
