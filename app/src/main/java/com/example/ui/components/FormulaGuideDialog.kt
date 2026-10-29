package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormulaGuideDialog(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("formula_guide_dialog")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Slovin's Formula Guide",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Theory, assumptions & statistical usage",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_guide_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close guide"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // What is Slovin's Formula Card
            GuideSection(
                title = "What is Slovin's Formula?",
                content = "Slovin's Formula (developed by Robert Slovin in 1960, and frequently referenced alongside Taro Yamane's 1967 simplified formula) is a widely utilized statistical technique for calculating an appropriate sample size (n) from a finite population (N) when little or no prior information is known about the population's variance or distribution."
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Mathematical Notation
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "n = N / (1 + N · e²)",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• n = Minimum required sample size\n• N = Total population size\n• e = Margin of error expressed as a decimal (e.g., 5% = 0.05)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // When to Use It
            GuideSection(
                title = "When Should You Use Slovin's Formula?",
                content = "1. Known Finite Population: When the exact total population count (N) is known (e.g., 10,000 university students, 5,000 employees, 25,000 community residents).\n\n2. Unknown Population Variance: When no prior historical data exists regarding the proportion (p) or standard deviation of the population.\n\n3. Simple Random Sampling: When every member in the population has an equal probability of being selected."
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Why Round UP?
            GuideSection(
                title = "Why do we always round UP (Ceiling)?",
                content = "In survey research, you cannot sample a fraction of a human or unit (e.g., 384.62 people). Always round up to the next integer (385) instead of down. Rounding down would cause the sample size to fall below the mathematical threshold required to maintain the desired margin of error, exceeding your targeted risk."
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Recommended Margins
            GuideSection(
                title = "Choosing Margin of Error (e)",
                content = "• 1% (0.01): Rigorous clinical trials, medical efficacy, safety compliance.\n• 2% – 3% (0.02 - 0.03): National election polls, government censuses, high-stakes market forecasting.\n• 5% (0.05): Standard accepted convention for university theses, academic research papers, and corporate surveys.\n• 10% (0.10): Exploratory surveys, rapid pilot testing, low-budget feedback polls."
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Stratified Allocation Section
            GuideSection(
                title = "Stratified Proportional Allocation",
                content = "When dividing a total sample size (n) among various strata or grade levels (e.g., Grade 7, 8, 9, 10), use proportional allocation:\n\nSample for Level = (Level Population / Total Population) × Sample Size\n\nThis ensures that larger grade levels represent a proportionately larger share of the sample, preventing sampling bias across grade levels."
            )
        }
    }
}

@Composable
private fun GuideSection(
    title: String,
    content: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )
        }
    }
}
