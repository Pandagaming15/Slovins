package com.example.model

import java.text.DecimalFormat
import kotlin.math.ceil
import kotlin.math.roundToLong

/**
 * Represents an individual mathematical step in Slovin's Formula computation.
 */
data class SlovinStep(
    val stepNumber: Int,
    val title: String,
    val formulaSymbolic: String,
    val substitution: String,
    val intermediateResult: String,
    val explanation: String,
    val highlightNote: String? = null
)

/**
 * Result of Slovin's formula calculation.
 */
data class SlovinCalculationResult(
    val populationN: Long,
    val marginOfErrorPercent: Double,
    val marginOfErrorDecimal: Double,
    val eSquared: Double,
    val nTimesESquared: Double,
    val denominator: Double,
    val rawSampleSize: Double,
    val recommendedSampleSize: Long, // Rounded up (ceiling)
    val nearestSampleSize: Long,     // Standard round
    val samplingRatioPercent: Double,
    val steps: List<SlovinStep>
)

/**
 * Row in the sensitivity comparison table.
 */
data class MarginComparisonItem(
    val marginPercent: Double,
    val sampleSize: Long,
    val rawSampleSize: Double,
    val samplingRatioPercent: Double,
    val precisionLabel: String,
    val isCurrent: Boolean
)

object SlovinMath {

    private val numberFormatter = DecimalFormat("#,###")
    private val decimalFormatter = DecimalFormat("#,##0.0000")
    private val shortDecimalFormatter = DecimalFormat("#,##0.00")

    fun formatNumber(value: Long): String = numberFormatter.format(value)
    fun formatNumber(value: Double): String = numberFormatter.format(value)
    fun formatDecimal(value: Double, decimals: Int = 4): String {
        return when (decimals) {
            2 -> shortDecimalFormatter.format(value)
            else -> decimalFormatter.format(value)
        }
    }

    /**
     * Computes Slovin's formula:
     * n = N / (1 + N * e^2)
     */
    fun calculate(population: Long, marginPercent: Double): SlovinCalculationResult? {
        if (population <= 0 || marginPercent <= 0.0 || marginPercent > 100.0) {
            return null
        }

        val e = marginPercent / 100.0
        val eSquared = e * e
        val nTimesESquared = population * eSquared
        val denominator = 1.0 + nTimesESquared
        val rawSampleSize = population / denominator
        val recommendedSampleSize = ceil(rawSampleSize).toLong()
        val nearestSampleSize = rawSampleSize.roundToLong()
        val samplingRatioPercent = (recommendedSampleSize.toDouble() / population.toDouble()) * 100.0

        val steps = listOf(
            SlovinStep(
                stepNumber = 1,
                title = "Identify Given Values",
                formulaSymbolic = "N, e",
                substitution = "N = ${formatNumber(population)}, e = ${marginPercent}%",
                intermediateResult = "e = ${e.toBigDecimal().toPlainString()}",
                explanation = "Convert the desired margin of error from percentage to decimal form: e = $marginPercent / 100 = ${e.toBigDecimal().toPlainString()}.",
                highlightNote = "Population (N) = ${formatNumber(population)}"
            ),
            SlovinStep(
                stepNumber = 2,
                title = "Square the Margin of Error",
                formulaSymbolic = "e²",
                substitution = "(${e.toBigDecimal().toPlainString()})²",
                intermediateResult = formatDecimal(eSquared, 6),
                explanation = "Square the margin of error (e²). Smaller error margins result in exponentially smaller squares, which increases sample size requirement.",
                highlightNote = "e² = ${formatDecimal(eSquared, 6)}"
            ),
            SlovinStep(
                stepNumber = 3,
                title = "Multiply Population by e²",
                formulaSymbolic = "N · e²",
                substitution = "${formatNumber(population)} × ${formatDecimal(eSquared, 6)}",
                intermediateResult = formatDecimal(nTimesESquared, 4),
                explanation = "Multiply the population size (N) by the squared margin of error (e²).",
                highlightNote = "N·e² = ${formatDecimal(nTimesESquared, 4)}"
            ),
            SlovinStep(
                stepNumber = 4,
                title = "Compute the Denominator",
                formulaSymbolic = "1 + N · e²",
                substitution = "1 + ${formatDecimal(nTimesESquared, 4)}",
                intermediateResult = formatDecimal(denominator, 4),
                explanation = "Add 1 to the product of N and e² to get the complete divisor.",
                highlightNote = "Denominator = ${formatDecimal(denominator, 4)}"
            ),
            SlovinStep(
                stepNumber = 5,
                title = "Divide Population by Denominator",
                formulaSymbolic = "n = N / (1 + N · e²)",
                substitution = "${formatNumber(population)} / ${formatDecimal(denominator, 4)}",
                intermediateResult = formatDecimal(rawSampleSize, 4),
                explanation = "Divide total population (N) by the calculated denominator. This yields the raw unrounded sample size.",
                highlightNote = "Exact n ≈ ${formatDecimal(rawSampleSize, 2)}"
            ),
            SlovinStep(
                stepNumber = 6,
                title = "Round Up (Statistical Recommendation)",
                formulaSymbolic = "⌈n⌉",
                substitution = "⌈${formatDecimal(rawSampleSize, 2)}⌉",
                intermediateResult = "$recommendedSampleSize respondents",
                explanation = "In research and survey statistics, always round up to the next whole person/entity (ceiling) to ensure that the error does not exceed the designated margin of error ($marginPercent%).",
                highlightNote = "Recommended Minimum = $recommendedSampleSize"
            )
        )

        return SlovinCalculationResult(
            populationN = population,
            marginOfErrorPercent = marginPercent,
            marginOfErrorDecimal = e,
            eSquared = eSquared,
            nTimesESquared = nTimesESquared,
            denominator = denominator,
            rawSampleSize = rawSampleSize,
            recommendedSampleSize = recommendedSampleSize,
            nearestSampleSize = nearestSampleSize,
            samplingRatioPercent = samplingRatioPercent,
            steps = steps
        )
    }

    /**
     * Generates a full text breakdown suitable for copying to clipboard or thesis/homework.
     */
    fun generateExportText(result: SlovinCalculationResult): String {
        return buildString {
            appendLine("=== SLOVIN'S FORMULA CALCULATION ===")
            appendLine("Formula: n = N / (1 + N · e²)")
            appendLine("------------------------------------")
            appendLine("GIVEN:")
            appendLine("  Population Size (N)   = ${formatNumber(result.populationN)}")
            appendLine("  Margin of Error (e)   = ${result.marginOfErrorPercent}% (${result.marginOfErrorDecimal})")
            appendLine()
            appendLine("STEP-BY-STEP SOLUTION:")
            result.steps.forEach { step ->
                appendLine("Step ${step.stepNumber}: ${step.title}")
                appendLine("  Formula:      ${step.formulaSymbolic}")
                appendLine("  Substitution: ${step.substitution}")
                appendLine("  Result:       ${step.intermediateResult}")
                appendLine("  Details:      ${step.explanation}")
                appendLine()
            }
            appendLine("FINAL RESULT:")
            appendLine("  Exact Sample Size:       ${formatDecimal(result.rawSampleSize, 3)}")
            appendLine("  Recommended Sample Size: ${result.recommendedSampleSize} respondents (Rounded Up)")
            appendLine("  Standard Rounded:        ${result.nearestSampleSize} respondents")
            appendLine("  Sampling Fraction:       ${formatDecimal(result.samplingRatioPercent, 2)}% of population")
            appendLine("------------------------------------")
            appendLine("Calculated via Slovin's Calculator App")
        }
    }

    /**
     * Computes sensitivity comparison across common research error margins.
     */
    fun computeComparison(population: Long, currentMargin: Double): List<MarginComparisonItem> {
        if (population <= 0) return emptyList()

        val margins = listOf(
            1.0 to "Very High (Medical/Clinical)",
            2.0 to "High (Scientific Research)",
            3.0 to "Standard High (National Polls)",
            5.0 to "Standard Academic (Thesis/Surveys)",
            7.0 to "Moderate (Market Research)",
            10.0 to "Exploratory (Pilot/Internal Study)"
        )

        return margins.map { (margin, label) ->
            val calc = calculate(population, margin)
            val sampleSize = calc?.recommendedSampleSize ?: 0L
            val raw = calc?.rawSampleSize ?: 0.0
            val ratio = if (population > 0) (sampleSize.toDouble() / population.toDouble()) * 100.0 else 0.0
            MarginComparisonItem(
                marginPercent = margin,
                sampleSize = sampleSize,
                rawSampleSize = raw,
                samplingRatioPercent = ratio,
                precisionLabel = label,
                isCurrent = Math.abs(currentMargin - margin) < 0.01
            )
        }
    }
}
