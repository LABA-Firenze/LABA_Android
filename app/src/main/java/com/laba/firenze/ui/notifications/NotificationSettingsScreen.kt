package com.laba.firenze.ui.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.laba.firenze.data.NotificationCategory
import com.laba.firenze.ui.notifications.viewmodel.NotificationSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    navController: NavController,
    viewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Notifiche") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Indietro")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Notifiche app (come iOS)
            item {
                NotificationSection(
                    title = "Notifiche app",
                    footer = "Disattivando le notifiche non riceverai le relative comunicazioni dall'Accademia. La responsabilità di restare informato è tua."
                ) {
                    NotificationToggleItem(
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = viewModel::setNotificationsEnabled,
                        title = "Abilita tutte le notifiche",
                        icon = Icons.Default.Notifications
                    )
                    if (!uiState.notificationsEnabled) {
                        NotificationCategory.entries.forEach { category ->
                            NotificationToggleItem(
                                checked = uiState.categoriesEnabled[category] == true,
                                onCheckedChange = { viewModel.setCategoryEnabled(category, it) },
                                title = category.displayName,
                                icon = getIconForCategory(category)
                            )
                        }
                    }
                }
            }

            // 2. Vita in LABA – Traguardi
            item {
                NotificationSection(
                    title = "Vita in LABA",
                    footer = "Notifiche sui punti traguardo e obiettivi."
                ) {
                    NotificationToggleItem(
                        checked = uiState.achievementNotificationsEnabled,
                        onCheckedChange = viewModel::setAchievementNotificationsEnabled,
                        title = "Traguardi",
                        icon = Icons.Default.EmojiEvents
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationSection(
    title: String,
    footer: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                content()
            }
        }
        if (footer != null) {
            Text(
                text = footer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun NotificationToggleItem(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        ListItem(
            headlineContent = { Text(title) },
            leadingContent = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingContent = {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        )
    }
}

private fun getIconForCategory(category: NotificationCategory): androidx.compose.ui.graphics.vector.ImageVector = when (category) {
    NotificationCategory.GENERAL -> Icons.Default.ChatBubble
    NotificationCategory.MATERIALS -> Icons.Default.Folder
    NotificationCategory.ABSENCES -> Icons.Default.PersonRemove
}
