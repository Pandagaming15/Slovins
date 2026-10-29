package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.CalculationEntity
import com.example.data.repository.CalculationRepository
import com.example.model.MarginComparisonItem
import com.example.model.SlovinCalculationResult
import com.example.model.SlovinMath
import com.example.model.StratifiedAllocationSummary
import com.example.model.StratumInputItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SlovinUiState(
    val populationInput: String = "10000",
    val marginInput: String = "5.0",
    val selectedMarginPreset: Double = 5.0,
    val calculationResult: SlovinCalculationResult? = null,
    val sensitivityList: List<MarginComparisonItem> = emptyList(),
    val isStepsExpanded: Boolean = true,
    val selectedStepIndex: Int? = null,
    val activeTab: Int = 0, // 0: Calculator & Steps, 1: Level / Strata Allocation, 2: Sensitivity Table, 3: Guide
    val snackbarMessage: String? = null,
    val showHistorySheet: Boolean = false,
    val showFormulaInfoDialog: Boolean = false,

    // Stratified / Grade Level Allocation State
    val strataInputs: List<StratumInputItem> = listOf(
        StratumInputItem(name = "Grade 7", populationStr = "350"),
        StratumInputItem(name = "Grade 8", populationStr = "300"),
        StratumInputItem(name = "Grade 9", populationStr = "250"),
        StratumInputItem(name = "Grade 10", populationStr = "200")
    ),
    val customSampleSizeInput: String = "",
    val useSlovinSampleSize: Boolean = true,
    val stratifiedSummary: StratifiedAllocationSummary? = null,
    val expandedStratumDetailId: String? = null
)

class SlovinViewModel(
    application: Application,
    private val repository: CalculationRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SlovinUiState())
    val uiState: StateFlow<SlovinUiState> = _uiState.asStateFlow()

    val historyList: StateFlow<List<CalculationEntity>> = repository.history
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Initial calculation with default 10,000 population and 5% margin
        recalculate("10000", "5.0")
        recomputeStratifiedAllocation()
    }

    fun onPopulationChange(newPopulation: String) {
        val clean = newPopulation.filter { it.isDigit() }
        recalculate(clean, _uiState.value.marginInput)
        recomputeStratifiedAllocation()
    }

    fun onMarginChange(newMargin: String) {
        val clean = newMargin.filter { it.isDigit() || it == '.' }
        val parts = clean.split('.')
        val formatted = if (parts.size > 2) {
            "${parts[0]}.${parts.drop(1).joinToString("")}"
        } else {
            clean
        }

        val marginDouble = formatted.toDoubleOrNull() ?: 0.0
        val matchedPreset = if (marginDouble in listOf(1.0, 2.0, 3.0, 5.0, 10.0)) marginDouble else 0.0

        _uiState.update { it.copy(selectedMarginPreset = matchedPreset) }
        recalculate(_uiState.value.populationInput, formatted)
        recomputeStratifiedAllocation()
    }

    fun setPresetPopulation(value: Long) {
        recalculate(value.toString(), _uiState.value.marginInput)
        recomputeStratifiedAllocation()
    }

    fun adjustPopulation(delta: Long) {
        val current = _uiState.value.populationInput.toLongOrNull() ?: 0L
        val updated = (current + delta).coerceAtLeast(1L)
        recalculate(updated.toString(), _uiState.value.marginInput)
        recomputeStratifiedAllocation()
    }

    fun setPresetMargin(percent: Double) {
        val formattedMargin = if (percent % 1.0 == 0.0) {
            percent.toInt().toString()
        } else {
            percent.toString()
        }
        _uiState.update { it.copy(selectedMarginPreset = percent) }
        recalculate(_uiState.value.populationInput, formattedMargin)
        recomputeStratifiedAllocation()
    }

    private fun recalculate(popStr: String, marginStr: String) {
        val population = popStr.toLongOrNull() ?: 0L
        val margin = marginStr.toDoubleOrNull() ?: 0.0

        val result = if (population > 0 && margin > 0.0 && margin <= 100.0) {
            SlovinMath.calculate(population, margin)
        } else {
            null
        }

        val sensitivity = if (population > 0) {
            SlovinMath.computeComparison(population, margin)
        } else {
            emptyList()
        }

        _uiState.update {
            it.copy(
                populationInput = popStr,
                marginInput = marginStr,
                calculationResult = result,
                sensitivityList = sensitivity
            )
        }
    }

    // ==========================================
    // STRATIFIED / GRADE LEVEL ALLOCATION LOGIC
    // ==========================================

    fun addStratum(name: String = "", population: String = "") {
        val currentList = _uiState.value.strataInputs.toMutableList()
        val defaultName = if (name.isBlank()) "Grade ${currentList.size + 7}" else name
        currentList.add(StratumInputItem(name = defaultName, populationStr = population))
        _uiState.update { it.copy(strataInputs = currentList) }
        recomputeStratifiedAllocation()
    }

    fun updateStratum(id: String, name: String, population: String) {
        val cleanPop = population.filter { it.isDigit() }
        val updated = _uiState.value.strataInputs.map { item ->
            if (item.id == id) item.copy(name = name, populationStr = cleanPop) else item
        }
        _uiState.update { it.copy(strataInputs = updated) }
        recomputeStratifiedAllocation()
    }

    fun removeStratum(id: String) {
        val current = _uiState.value.strataInputs
        if (current.size <= 1) {
            _uiState.update { it.copy(snackbarMessage = "At least one level is required") }
            return
        }
        val updated = current.filterNot { it.id == id }
        _uiState.update { it.copy(strataInputs = updated) }
        recomputeStratifiedAllocation()
    }

    fun toggleUseSlovinSample(useSlovin: Boolean) {
        _uiState.update { it.copy(useSlovinSampleSize = useSlovin) }
        recomputeStratifiedAllocation()
    }

    fun onCustomSampleSizeChange(newSample: String) {
        val clean = newSample.filter { it.isDigit() }
        _uiState.update { it.copy(customSampleSizeInput = clean) }
        recomputeStratifiedAllocation()
    }

    fun toggleStratumDetail(id: String) {
        _uiState.update {
            it.copy(expandedStratumDetailId = if (it.expandedStratumDetailId == id) null else id)
        }
    }

    fun syncStrataSumToSlovinPopulation() {
        val sum = _uiState.value.strataInputs.sumOf {
            it.populationStr.filter { c -> c.isDigit() }.toLongOrNull() ?: 0L
        }
        if (sum > 0L) {
            onPopulationChange(sum.toString())
            _uiState.update {
                it.copy(snackbarMessage = "Synced ${SlovinMath.formatNumber(sum)} to main Slovin Population!")
            }
        } else {
            _uiState.update { it.copy(snackbarMessage = "Enter level populations first") }
        }
    }

    fun applyStrataPreset(preset: String) {
        val newList = when (preset) {
            "junior_high" -> listOf(
                StratumInputItem(name = "Grade 7", populationStr = "350"),
                StratumInputItem(name = "Grade 8", populationStr = "320"),
                StratumInputItem(name = "Grade 9", populationStr = "280"),
                StratumInputItem(name = "Grade 10", populationStr = "250")
            )
            "complete_secondary" -> listOf(
                StratumInputItem(name = "Grade 7", populationStr = "350"),
                StratumInputItem(name = "Grade 8", populationStr = "320"),
                StratumInputItem(name = "Grade 9", populationStr = "280"),
                StratumInputItem(name = "Grade 10", populationStr = "250"),
                StratumInputItem(name = "Grade 11", populationStr = "220"),
                StratumInputItem(name = "Grade 12", populationStr = "180")
            )
            "college" -> listOf(
                StratumInputItem(name = "1st Year", populationStr = "500"),
                StratumInputItem(name = "2nd Year", populationStr = "420"),
                StratumInputItem(name = "3rd Year", populationStr = "350"),
                StratumInputItem(name = "4th Year", populationStr = "280")
            )
            "departments" -> listOf(
                StratumInputItem(name = "Engineering", populationStr = "450"),
                StratumInputItem(name = "Business", populationStr = "600"),
                StratumInputItem(name = "Arts & Sciences", populationStr = "380"),
                StratumInputItem(name = "Education", populationStr = "270")
            )
            else -> listOf(
                StratumInputItem(name = "Grade 7", populationStr = "350"),
                StratumInputItem(name = "Grade 8", populationStr = "300")
            )
        }
        _uiState.update { it.copy(strataInputs = newList) }
        recomputeStratifiedAllocation()
    }

    private fun recomputeStratifiedAllocation() {
        val state = _uiState.value
        val effectiveSampleSize = if (state.useSlovinSampleSize) {
            state.calculationResult?.recommendedSampleSize ?: 0L
        } else {
            state.customSampleSizeInput.toLongOrNull() ?: 0L
        }

        val summary = if (effectiveSampleSize > 0L) {
            SlovinMath.calculateStratifiedAllocation(
                strataInputs = state.strataInputs,
                totalSampleSize = effectiveSampleSize
            )
        } else {
            null
        }

        _uiState.update { it.copy(stratifiedSummary = summary) }
    }

    fun copyStratifiedReport(context: Context) {
        val summary = _uiState.value.stratifiedSummary ?: return
        val text = SlovinMath.generateStratifiedExportText(summary)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Stratified Allocation Report", text)
        clipboard.setPrimaryClip(clip)
        _uiState.update { it.copy(snackbarMessage = "Stratified allocation report copied!") }
    }

    // ==========================================
    // UI NAVIGATION & INTERACTION
    // ==========================================

    fun toggleStepsExpanded() {
        _uiState.update { it.copy(isStepsExpanded = !it.isStepsExpanded) }
    }

    fun selectStep(index: Int?) {
        _uiState.update { it.copy(selectedStepIndex = if (it.selectedStepIndex == index) null else index) }
    }

    fun setActiveTab(tab: Int) {
        _uiState.update { it.copy(activeTab = tab) }
        if (tab == 1) {
            recomputeStratifiedAllocation()
        }
    }

    fun toggleHistorySheet(show: Boolean) {
        _uiState.update { it.copy(showHistorySheet = show) }
    }

    fun toggleFormulaInfoDialog(show: Boolean) {
        _uiState.update { it.copy(showFormulaInfoDialog = show) }
    }

    fun saveCurrentCalculation() {
        val result = _uiState.value.calculationResult ?: return
        viewModelScope.launch {
            repository.saveCalculation(
                population = result.populationN,
                marginPercent = result.marginOfErrorPercent,
                rawResult = result.rawSampleSize,
                roundedResult = result.recommendedSampleSize
            )
            _uiState.update { it.copy(snackbarMessage = "Calculation saved to history!") }
        }
    }

    fun loadFromHistory(item: CalculationEntity) {
        val marginStr = if (item.marginOfErrorPercent % 1.0 == 0.0) {
            item.marginOfErrorPercent.toInt().toString()
        } else {
            item.marginOfErrorPercent.toString()
        }
        recalculate(item.population.toString(), marginStr)
        recomputeStratifiedAllocation()
        _uiState.update {
            it.copy(
                showHistorySheet = false,
                snackbarMessage = "Loaded calculation (N = ${SlovinMath.formatNumber(item.population)}, e = $marginStr%)"
            )
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteCalculation(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _uiState.update { it.copy(snackbarMessage = "History cleared") }
        }
    }

    fun copyStepsToClipboard(context: Context) {
        val result = _uiState.value.calculationResult ?: return
        val text = SlovinMath.generateExportText(result)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Slovin Solution Steps", text)
        clipboard.setPrimaryClip(clip)
        _uiState.update { it.copy(snackbarMessage = "Complete step-by-step solution copied!") }
    }

    fun copyResultOnly(context: Context) {
        val result = _uiState.value.calculationResult ?: return
        val text = "Recommended Sample Size: ${result.recommendedSampleSize} respondents (N=${SlovinMath.formatNumber(result.populationN)}, e=${result.marginOfErrorPercent}%)"
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Slovin Result", text)
        clipboard.setPrimaryClip(clip)
        _uiState.update { it.copy(snackbarMessage = "Sample size $text copied!") }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = AppDatabase.getDatabase(application)
                    val repo = CalculationRepository(db.calculationDao())
                    return SlovinViewModel(application, repo) as T
                }
            }
    }
}
