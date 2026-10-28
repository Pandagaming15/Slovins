package com.example

import com.example.model.SlovinMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun slovinCalculation_isCorrect() {
        // N = 10,000, e = 5% (0.05)
        // 10000 / (1 + 10000 * 0.0025) = 10000 / 26 = 384.615...
        // recommended = 385
        val result = SlovinMath.calculate(10000L, 5.0)
        assertNotNull(result)
        result?.let {
            assertEquals(10000L, it.populationN)
            assertEquals(5.0, it.marginOfErrorPercent, 0.001)
            assertEquals(0.05, it.marginOfErrorDecimal, 0.0001)
            assertEquals(0.0025, it.eSquared, 0.00001)
            assertEquals(26.0, it.denominator, 0.001)
            assertEquals(385L, it.recommendedSampleSize)
            assertEquals(385L, it.nearestSampleSize)
            assertEquals(6, it.steps.size)
        }
    }

    @Test
    fun slovinCalculation_stepsVerification() {
        val result = SlovinMath.calculate(5000L, 2.0)
        assertNotNull(result)
        result?.let {
            assertTrue(it.recommendedSampleSize > 0)
            assertEquals(6, it.steps.size)
            assertEquals(1, it.steps[0].stepNumber)
            assertEquals(6, it.steps[5].stepNumber)
        }
    }
}
