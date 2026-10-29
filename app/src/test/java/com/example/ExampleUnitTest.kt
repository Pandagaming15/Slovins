package com.example

import com.example.model.SlovinMath
import com.example.model.StratumInputItem
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

    @Test
    fun stratifiedAllocation_isCorrect() {
        // Grade 7 (250), Grade 8 (250), Grade 9 (250), Grade 10 (250)
        // Total N = 1000, Total Sample Size = 286
        val strata = listOf(
            StratumInputItem(name = "Grade 7", populationStr = "250"),
            StratumInputItem(name = "Grade 8", populationStr = "250"),
            StratumInputItem(name = "Grade 9", populationStr = "250"),
            StratumInputItem(name = "Grade 10", populationStr = "250")
        )

        val summary = SlovinMath.calculateStratifiedAllocation(
            strataInputs = strata,
            totalSampleSize = 286L
        )

        assertNotNull(summary)
        summary?.let {
            assertEquals(1000L, it.totalPopulation)
            assertEquals(286L, it.totalSampleSize)
            assertEquals(4, it.strata.size)
            // Each stratum has (250 / 1000) * 286 = 71.5
            // Two will get 72 and two will get 71, summing to 286 exactly
            assertEquals(286L, it.totalAllocatedSample)
            val g7 = it.strata.first { s -> s.name == "Grade 7" }
            assertEquals(0.25, g7.proportionRatio, 0.0001)
            assertEquals(25.0, g7.proportionPercent, 0.0001)
            assertEquals(71.5, g7.rawAllocatedSample, 0.001)
            assertTrue(g7.substitutionDisplay.contains("250"))
            assertTrue(g7.substitutionDisplay.contains("1,000"))
            assertTrue(g7.substitutionDisplay.contains("286"))
        }
    }

    @Test
    fun stratifiedAllocation_unequalGrades() {
        // Grade 7: 400 (40%), Grade 8: 300 (30%), Grade 9: 200 (20%), Grade 10: 100 (10%)
        // Total N = 1000, n = 100
        val strata = listOf(
            StratumInputItem(name = "Grade 7", populationStr = "400"),
            StratumInputItem(name = "Grade 8", populationStr = "300"),
            StratumInputItem(name = "Grade 9", populationStr = "200"),
            StratumInputItem(name = "Grade 10", populationStr = "100")
        )

        val summary = SlovinMath.calculateStratifiedAllocation(
            strataInputs = strata,
            totalSampleSize = 100L
        )

        assertNotNull(summary)
        summary?.let {
            assertEquals(40L, it.strata[0].allocatedSampleSize)
            assertEquals(30L, it.strata[1].allocatedSampleSize)
            assertEquals(20L, it.strata[2].allocatedSampleSize)
            assertEquals(10L, it.strata[3].allocatedSampleSize)
            assertEquals(100L, it.totalAllocatedSample)
        }
    }
}
