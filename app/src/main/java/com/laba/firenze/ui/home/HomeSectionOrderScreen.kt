package com.laba.firenze.ui.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

/** Nomi sezioni per l'ordine Home (identico a iOS home.sectionOrder). */
private val SECTION_LABELS = mapOf(
    "hero" to "Hero / Benvenuto",
    "kpi" to "KPI (Esami, CFA)",
    "progress" to "Progresso anno e media",
    "lessons" to "Lezioni di oggi",
    "exams" to "Esami prenotati",
    "quickActions" to "Per te e Servizi"
)

/** Schermata ordine sezioni Home con drag & drop (identico a iOS personalizzazione). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSectionOrderScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val order by viewModel.sectionOrder.collectAsStateWithLifecycle()
    var currentOrder by remember(order) { mutableStateOf(order) }

    LaunchedEffect(order) {
        currentOrder = order
    }

    val reorderableState = rememberReorderableLazyListState(
        onMove = { from, to ->
            // Indici offset di 1 per via dell'item header "Trascina per riordinare..."
            val fromIdx = (from.index - 1).coerceIn(0, currentOrder.lastIndex)
            val toIdx = (to.index - 1).coerceIn(0, currentOrder.lastIndex)
            if (fromIdx != toIdx) {
                currentOrder = currentOrder.toMutableList().apply {
                    add(toIdx, removeAt(fromIdx))
                }
                viewModel.saveSectionOrder(currentOrder)
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ordine sezioni Bacheca") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = reorderableState.listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .reorderable(reorderableState)
                .detectReorderAfterLongPress(reorderableState),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Trascina per riordinare le sezioni nella Home.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(currentOrder, key = { it }) { sectionId ->
                ReorderableItem(reorderableState, key = sectionId) { isDragging ->
                    val elevation by animateDpAsState(
                        if (isDragging) 8.dp else 0.dp,
                        label = "elevation"
                    )
                    val label = SECTION_LABELS[sectionId] ?: sectionId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.DragHandle,
                                contentDescription = "Trascina",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
