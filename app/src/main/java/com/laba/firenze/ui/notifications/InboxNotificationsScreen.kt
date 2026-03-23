package com.laba.firenze.ui.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.laba.firenze.ui.notifications.viewmodel.InboxNotificationsViewModel
import com.laba.firenze.ui.notifications.viewmodel.InboxNotificationsViewModel.NotificationDisplayItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxNotificationsScreen(
    navController: NavController,
    viewModel: InboxNotificationsViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val filteredNotifications by viewModel.filteredNotifications.collectAsState()
    val showOnlyUnread by viewModel.showOnlyUnread.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedNotification by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifiche") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Indietro")
                    }
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        var showMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "Azioni")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Segna tutte come lette") },
                                leadingIcon = { Icon(Icons.Default.CheckCircle, null) },
                                onClick = { viewModel.markAllAsRead(); showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Elimina tutte") },
                                leadingIcon = { Icon(Icons.Default.Delete, null) },
                                onClick = { /* TODO */; showMenu = false }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header "Avvisi sui caricamenti" → lista completa (come iOS 5.1.1)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* già sulla lista completa */ },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                "Avvisi sui caricamenti",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Vai alla lista completa",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Toggle "Solo non lette" + azioni (come iOS 5.1.3)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.MarkEmailUnread,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Solo non lette", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = showOnlyUnread,
                            onCheckedChange = { viewModel.setShowOnlyUnread(it) }
                        )
                    }
                }
            }
            
            if (filteredNotifications.isEmpty() && !isLoading) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsOff,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Nessuna notifica",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(
                    items = filteredNotifications,
                    key = { it.id }
                ) { notification ->
                    NotificationCard(
                        notification = notification,
                        onClick = { selectedNotification = notification.id },
                        onDismiss = { viewModel.dismiss(notification.id) },
                        onMarkRead = { viewModel.setRead(notification.id, true) },
                        onMarkUnread = { viewModel.setRead(notification.id, false) }
                    )
                }
            }
        }
        
        // Detail sheet
        selectedNotification?.let { id ->
            val notification = notifications.find { it.id == id }
            if (notification != null) {
                NotificationDetailSheet(
                    notification = notification,
                    onDismiss = { selectedNotification = null },
                    onReadChanged = { read -> viewModel.setRead(id, read) },
                    onDelete = {
                        viewModel.dismiss(id)
                        selectedNotification = null
                    }
                )
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: NotificationDisplayItem,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    onMarkRead: () -> Unit,
    onMarkUnread: () -> Unit
) {
    var showContextMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icona letta (checkmark.seal.fill) vs non letta (envelope.badge) - come iOS 5.2.3
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (notification.isRead) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }
            ) {
                Box(
                    modifier = Modifier.size(34.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (notification.isRead) Icons.Default.CheckCircle else Icons.Outlined.MarkEmailUnread,
                        contentDescription = null,
                        tint = if (notification.isRead) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold,
                        maxLines = 2,
                        lineHeight = MaterialTheme.typography.titleMedium.lineHeight
                    )
                    IconButton(
                        onClick = { showContextMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Azioni",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = notification.dateString,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.25f
                )
            }
        }
    }
    
    DropdownMenu(
        expanded = showContextMenu,
        onDismissRequest = { showContextMenu = false }
    ) {
        if (notification.isRead) {
            DropdownMenuItem(
                text = { Text("Da leggere") },
                leadingIcon = { Icon(Icons.Outlined.MarkEmailUnread, null) },
                onClick = { onMarkUnread(); showContextMenu = false }
            )
        } else {
            DropdownMenuItem(
                text = { Text("Letta") },
                leadingIcon = { Icon(Icons.Default.CheckCircle, null) },
                onClick = { onMarkRead(); showContextMenu = false }
            )
        }
        DropdownMenuItem(
            text = { Text("Elimina") },
            leadingIcon = { Icon(Icons.Default.Delete, null) },
            onClick = { onDismiss(); showContextMenu = false },
            colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.error)
        )
    }
}
