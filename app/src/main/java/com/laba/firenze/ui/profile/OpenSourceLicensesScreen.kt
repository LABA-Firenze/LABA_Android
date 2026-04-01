package com.laba.firenze.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

private data class OpenSourceLicenseItem(
    val name: String,
    val title: String,
    val license: String,
    val url: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenSourceLicensesScreen(navController: NavController) {
    val uriHandler = LocalUriHandler.current
    val items = listOf(
        OpenSourceLicenseItem("Firebase Android SDK", "Push/Analytics", "Apache 2.0", "https://github.com/firebase/firebase-android-sdk"),
        OpenSourceLicenseItem("Retrofit", "HTTP API client", "Apache 2.0", "https://github.com/square/retrofit"),
        OpenSourceLicenseItem("OkHttp", "HTTP transport", "Apache 2.0", "https://github.com/square/okhttp"),
        OpenSourceLicenseItem("Hilt (Dagger)", "Dependency injection", "Apache 2.0", "https://github.com/google/dagger"),
        OpenSourceLicenseItem("Room", "Local database", "Apache 2.0", "https://developer.android.com/jetpack/androidx/releases/room"),
        OpenSourceLicenseItem("Coil", "Image loading", "Apache 2.0", "https://github.com/coil-kt/coil"),
        OpenSourceLicenseItem("ZXing", "QR generation", "Apache 2.0", "https://github.com/zxing/zxing"),
        OpenSourceLicenseItem("JJWT", "JWT parsing/signing", "Apache 2.0", "https://github.com/jwtk/jjwt"),
        OpenSourceLicenseItem("Compose Reorderable", "Reorder UI lists", "Apache 2.0", "https://github.com/aclassen/ComposeReorderable"),
        OpenSourceLicenseItem("Dizionario italiano Hunspell", "Italian.aff / Italian.dic bundled dictionary", "GPLv3+", "https://sourceforge.net/projects/linguistico/"),
        OpenSourceLicenseItem("GNU GPL v3 (testo licenza)", "Testo completo della licenza GPL", "GPLv3", "https://www.gnu.org/licenses/gpl-3.0.txt")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Licenze Open Source") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "Elenco librerie e dipendenze principali usate nell'app, con tipo di licenza e link di riferimento.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
            item {
                Text(
                    text = "Nota compliance: per la componente GPL (dizionario Hunspell italiano), la licenza completa e i riferimenti sorgente sono disponibili ai link in elenco. Contatto: info@laba.biz",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            items(items) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        ListItem(
                            headlineContent = {
                                Text(item.name, fontWeight = FontWeight.SemiBold)
                            },
                            supportingContent = {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(item.title)
                                    Text("Licenza: ${item.license}", fontWeight = FontWeight.Medium)
                                }
                            },
                            trailingContent = {
                                if (!item.url.isNullOrBlank()) {
                                    IconButton(onClick = { uriHandler.openUri(item.url) }) {
                                        Icon(Icons.Default.Link, contentDescription = "Apri link")
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
