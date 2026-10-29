package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.SlovinMath
import com.example.ui.components.FormulaDisplayCard
import com.example.ui.components.FormulaGuideDialog
import com.example.ui.components.HistoryBottomSheet
import com.example.ui.components.MarginOfErrorCard
import com.example.ui.components.PopulationInputCard
import com.example.ui.components.ResultSummaryCard
import com.example.ui.components.SensitivityTableCard
import com.example.ui.components.StepByStepCard
import com.example.ui.components.StratifiedAllocationCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlovinMainScreen(
    viewModel: SlovinViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissSnackbar()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "n",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                ),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Slovin's Calculator",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleHistorySheet(true) },
                        modifier = Modifier.testTag("open_history_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (historyList.isNotEmpty()) {
                                    Badge {
                                        Text(historyList.size.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = "History",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.toggleFormulaInfoDialog(true) },
                        modifier = Modifier.testTag("open_guide_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.School,
                            contentDescription = "Formula Guide",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Navigation Tabs (Scrollable to comfortably fit 4 tabs)
            ScrollableTabRow(
                selectedTabIndex = uiState.activeTab,
                edgePadding = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("main_tabs")
            ) {
                Tab(
                    selected = uiState.activeTab == 0,
                    onClick = { viewModel.setActiveTab(0) },
                    text = { Text("Slovin Formula") },
                    icon = { Icon(Icons.Filled.Calculate, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_calculator")
                )
                Tab(
                    selected = uiState.activeTab == 1,
                    onClick = { viewModel.setActiveTab(1) },
                    text = { Text("Level Allocation") },
                    icon = { Icon(Icons.Filled.Layers, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_strata")
                )
                Tab(
                    selected = uiState.activeTab == 2,
                    onClick = { viewModel.setActiveTab(2) },
                    text = { Text("Sensitivity") },
                    icon = { Icon(Icons.Filled.TableView, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_sensitivity")
                )
                Tab(
                    selected = uiState.activeTab == 3,
                    onClick = { viewModel.setActiveTab(3) },
                    text = { Text("Guide") },
                    icon = { Icon(Icons.Filled.School, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_guide")
                )
            }

            // Tab Content Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.TopCenter
            ) {
                AnimatedContent(
                    targetState = uiState.activeTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_content"
                ) { tab ->
                    when (tab) {
                        0 -> CalculatorTabContent(
                            uiState = uiState,
                            viewModel = viewModel,
                            onCopySteps = { viewModel.copyStepsToClipboard(context) },
                            onGoToStrata = { viewModel.setActiveTab(1) }
                        )
                        1 -> StrataTabContent(
                            uiState = uiState,
                            viewModel = viewModel,
                            onCopyReport = { viewModel.copyStratifiedReport(context) }
                        )
                        2 -> SensitivityTabContent(
                            uiState = uiState,
                            onSelectMargin = { margin ->
                                viewModel.setPresetMargin(margin)
                                viewModel.setActiveTab(0)
                            }
                        )
                        3 -> GuideTabContent()
                    }
                }
            }
        }
    }

    // History Bottom Sheet
    if (uiState.showHistorySheet) {
        HistoryBottomSheet(
            historyList = historyList,
            onLoadItem = { entity -> viewModel.loadFromHistory(entity) },
            onDeleteItem = { id -> viewModel.deleteHistoryItem(id) },
            onClearAll = { viewModel.clearAllHistory() },
            onDismiss = { viewModel.toggleHistorySheet(false) }
        )
    }

    // Formula Guide Dialog
    if (uiState.showFormulaInfoDialog) {
        FormulaGuideDialog(
            onDismiss = { viewModel.toggleFormulaInfoDialog(false) }
        )
    }
}

@Composable
private fun CalculatorTabContent(
    uiState: SlovinUiState,
    viewModel: SlovinViewModel,
    onCopySteps: () -> Unit,
    onGoToStrata: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Interactive Formula Hero Card
        item {
            FormulaDisplayCard(
                result = uiState.calculationResult,
                onInfoClick = { viewModel.toggleFormulaInfoDialog(true) }
            )
        }

        // 2. Population Input Card
        item {
            PopulationInputCard(
                populationInput = uiState.populationInput,
                onPopulationChange = { viewModel.onPopulationChange(it) },
                onAdjustPopulation = { viewModel.adjustPopulation(it) },
                onSelectPreset = { viewModel.setPresetPopulation(it) }
            )
        }

        // 3. Margin of Error Input Card
        item {
            MarginOfErrorCard(
                marginInput = uiState.marginInput,
                selectedPreset = uiState.selectedMarginPreset,
                onMarginChange = { viewModel.onMarginChange(it) },
                onSelectPreset = { viewModel.setPresetMargin(it) }
            )
        }

        // 4. Hero Result Card
        item {
            ResultSummaryCard(
                result = uiState.calculationResult,
                onSaveClick = { viewModel.saveCurrentCalculation() },
                onCopyClick = onCopySteps
            )
        }

        // 5. Quick Stratified Allocation Callout
        if (uiState.calculationResult != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Distribute Across Grade Levels?",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Use (Level Pop / Total Pop) × ${uiState.calculationResult.recommendedSampleSize} to find sample size for Grade 7, 8, etc.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        FilledTonalButton(
                            onClick = onGoToStrata,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("go_to_strata_button")
                        ) {
                            Text("Allocate")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 6. Explicit Step-by-Step Breakdown
        item {
            StepByStepCard(
                result = uiState.calculationResult,
                isExpanded = uiState.isStepsExpanded,
                onToggleExpand = { viewModel.toggleStepsExpanded() },
                selectedStepIndex = uiState.selectedStepIndex,
                onSelectStep = { viewModel.selectStep(it) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StrataTabContent(
    uiState: SlovinUiState,
    viewModel: SlovinViewModel,
    onCopyReport: () -> Unit
) {
    val sampleSize = uiState.calculationResult?.recommendedSampleSize ?: 385L

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StratifiedAllocationCard(
                strataInputs = uiState.strataInputs,
                summary = uiState.stratifiedSummary,
                useSlovinSample = uiState.useSlovinSampleSize,
                customSampleInput = uiState.customSampleSizeInput,
                slovinCalculatedSampleSize = sampleSize,
                expandedDetailId = uiState.expandedStratumDetailId,
                onAddStratum = { viewModel.addStratum() },
                onUpdateStratum = { id, name, pop -> viewModel.updateStratum(id, name, pop) },
                onRemoveStratum = { id -> viewModel.removeStratum(id) },
                onToggleUseSlovin = { useSlovin -> viewModel.toggleUseSlovinSample(useSlovin) },
                onCustomSampleChange = { customVal -> viewModel.onCustomSampleSizeChange(customVal) },
                onToggleDetail = { id -> viewModel.toggleStratumDetail(id) },
                onApplyPreset = { preset -> viewModel.applyStrataPreset(preset) },
                onSyncPopulation = { viewModel.syncStrataSumToSlovinPopulation() },
                onCopyReport = onCopyReport
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SensitivityTabContent(
    uiState: SlovinUiState,
    onSelectMargin: (Double) -> Unit
) {
    val popLong = uiState.populationInput.toLongOrNull() ?: 0L

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SensitivityTableCard(
                population = popLong,
                items = uiState.sensitivityList,
                onSelectMargin = onSelectMargin
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Why Sensitivity Matters",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Reducing the margin of error from 5% to 1% exponentially increases the sample size needed (often by 10x or more). Use this sensitivity table to balance statistical precision against survey budget, timeline, and logistics.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideTabContent() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Slovin's Formula Overview
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "1. Slovin's Formula Overview",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Slovin's Formula calculates the overall minimum sample size (n) required from a known population (N) with a chosen margin of error (e):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "n = N / (1 + N · e²)",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Stratified Sampling / Grade Levels Formula
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "2. Stratified Proportional Allocation",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "When your population has distinct sub-groups or grade levels (e.g., Grade 7, Grade 8, Grade 9, Grade 10), each level's sample size is proportional to its representation in the total population:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Sample for Level = (Level Population / Total Population) × Sample Size",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "For example: if Grade 7 has 250 students out of 1,000 total students and the target sample is 286:\nSample for Grade 7 = (250 / 1,000) × 286 = 71.5 ≈ 72 respondents.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Why Round UP (Ceiling)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "3. Why Always Round Up (Ceiling)?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Because you cannot survey a fractional respondent (e.g. 384.62 people), rounding down would cause the resulting sample to drop below the mathematical minimum, exceeding your allowed margin of error. Always round up to ensure statistical confidence.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
