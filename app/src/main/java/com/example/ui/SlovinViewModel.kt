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
    val activeTab: Int = 0, // 0: Calculator & Steps, 1: Sensitivity Table, 2: Formula Guide
    val snackbarMessage: String? = null,
    val showHistorySheet: Boolean = false,
    val showFormulaInfoDialog: Boolean = false
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
    }

    fun onPopulationChange(newPopulation: String) {
        // Allow digits only
        val clean = newPopulation.filter { it.isDigit() }
        recalculate(clean, _uiState.value.marginInput)
    }

    fun onMarginChange(newMargin: String) {
        // Allow digits and up to one decimal point
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
    }

    fun setPresetPopulation(value: Long) {
        recalculate(value.toString(), _uiState.value.marginInput)
    }

    fun adjustPopulation(delta: Long) {
        val current = _uiState.value.populationInput.toLongOrNull() ?: 0L
        val updated = (current + delta).coerceAtLeast(1L)
        recalculate(updated.toString(), _uiState.value.marginInput)
    }

    fun setPresetMargin(percent: Double) {
        val formattedMargin = if (percent % 1.0 == 0.0) {
            percent.toInt().toString()
        } else {
            percent.toString()
        }
        _uiState.update { it.copy(selectedMarginPreset = percent) }
        recalculate(_uiState.value.populationInput, formattedMargin)
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

    fun toggleStepsExpanded() {
        _uiState.update { it.copy(isStepsExpanded = !it.isStepsExpanded) }
    }

    fun selectStep(index: Int?) {
        _uiState.update { it.copy(selectedStepIndex = if (it.selectedStepIndex == index) null else index) }
    }

    fun setActiveTab(tab: Int) {
        _uiState.update { it.copy(activeTab = tab) }
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
