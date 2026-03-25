package com.laba.firenze.ui.thesis

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

private const val LABA_PHONE_TEL = "+390556530786"
private const val LABA_PHONE_DISPLAY = "055 653 0786"
private const val LABA_EMAIL = "info@laba.biz"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PergamenaScreen(
    navController: NavController
) {
    var selectedPane by remember { mutableStateOf(PergamenaPane.Overview) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(selectedPane) {
        scrollState.scrollTo(0)
    }

    fun downloadFacsimile() {
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://laba.biz/wp-content/uploads/2025/03/FACSIMILE-BOLLETTINO-POSTALE.pdf")
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Exception) { }
    }

    fun dialSegreteria() {
        try {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$LABA_PHONE_TEL")))
        } catch (_: Exception) { }
    }

    fun emailSegreteria() {
        try {
            context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$LABA_EMAIL")))
        } catch (_: Exception) { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pergamena ufficiale") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SegmentedButton(
                    selected = selectedPane == PergamenaPane.Overview,
                    onClick = { selectedPane = PergamenaPane.Overview },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                    label = { Text("Panoramica") }
                )
                SegmentedButton(
                    selected = selectedPane == PergamenaPane.Pagamento,
                    onClick = { selectedPane = PergamenaPane.Pagamento },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                    label = { Text("Pagamento") }
                )
                SegmentedButton(
                    selected = selectedPane == PergamenaPane.Ritiro,
                    onClick = { selectedPane = PergamenaPane.Ritiro },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                    label = { Text("Ritiro") }
                )
            }

            HorizontalDivider()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .padding(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                when (selectedPane) {
                    PergamenaPane.Overview -> OverviewPane()
                    PergamenaPane.Pagamento -> PagamentoPane(onDownloadFacsimile = { downloadFacsimile() })
                    PergamenaPane.Ritiro -> RitiroPane(
                        onDial = { dialSegreteria() },
                        onEmail = { emailSegreteria() }
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewPane() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Richiesta e ritiro del diploma ufficiale",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Bollettino e marca da bollo servono in momenti diversi: il bollettino paga l'avvio della richiesta; la marca da bollo si presenta solo il giorno del ritiro, dopo appuntamento.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard2Line(
                    value = "€ 90,86",
                    caption = "Bollettino c/c 1016",
                    subtitle = "Per avviare la richiesta",
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
                KpiCard2Line(
                    value = "€ 16",
                    caption = "Marca da bollo",
                    subtitle = "Solo al ritiro in sede",
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }

    OverviewAppointmentBanner()

    InBreveOverviewCard()

    TextBlockCard(
        title = "Certificato Sostitutivo",
        body = "Una volta completato il pagamento e inviata la richiesta, la Segreteria Didattica potrà rilasciare in tempi brevi un Certificato Sostitutivo che ha lo stesso valore legale della pergamena e il Diploma Supplement in italiano e in inglese contenente anche tutti gli esami sostenuti, poiché i tempi di consegna della pergamena potrebbero essere lunghi."
    )

    TextBlockCard(
        title = "Cos'è il Diploma Supplement?",
        body = "È un documento ufficiale europeo che accompagna il diploma ufficiale. Riporta in modo chiaro il percorso di studi svolto in lingua Italiana e Inglese (corsi, esami, crediti, livello della qualifica) ed è pensato per facilitare il riconoscimento del titolo in Italia e all'estero, sia in ambito accademico che professionale."
    )
}

@Composable
private fun PagamentoPane(onDownloadFacsimile: () -> Unit) {
    Text(
        text = "Pagamento e documenti per la richiesta",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
    )

    InfoCard(title = "Bollettino (fase richiesta)", icon = Icons.Default.Description) {
        Text(
            text = "Questo versamento serve ad avviare la pratica di richiesta pergamena presso gli enti competenti.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Conto", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("c/c 1016", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Intestazione", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "AGENZIA DELLE ENTRATE – CENTRO OPERATIVO DI PESCARA – TASSE SCOLASTICHE",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    InfoCard(title = "Causale", icon = Icons.Default.TextFields) {
        Text("Indica chiaramente:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        listOf(
            "richiesta di diploma (primo o secondo livello)",
            "nome, cognome e indirizzo del diplomato",
            "percorso accademico"
        ).forEach { line ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Text(line, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    InfoCard(title = "Fac‑simile bollettino", icon = Icons.Default.Download) {
        Button(
            onClick = onDownloadFacsimile,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Download, null, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Scarica fac‑simile")
        }
    }

    InfoCard(title = "Cosa consegnare in Segreteria", icon = Icons.Default.Upload) {
        Text(
            "Per la richiesta porta la ricevuta del bollettino e il modulo compilato.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "La marca da bollo da € 16 non serve in questa fase: va portata fisicamente il giorno del ritiro, dopo aver concordato l'appuntamento (vedi scheda Ritiro).",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    InfoCard(title = "Note", icon = Icons.Default.Info) {
        Text(
            "Gli importi possono variare in base a disposizioni degli organi statali competenti.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RitiroPane(
    onDial: () -> Unit,
    onEmail: () -> Unit
) {
    InfoCard(title = "Appuntamento obbligatorio", icon = Icons.Default.CalendarMonth) {
        Text(
            "Il ritiro della pergamena avviene solo su appuntamento, presso la sede di Piazza di Badia a Ripoli 1/A (Firenze).",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Contatta la Segreteria per fissare giorno e ora. Al ritiro presenta la marca da bollo da € 16 (fisica), come previsto dalla normativa.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Contatta la Segreteria", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Telefono fisso LABA Firenze o email per concordare il ritiro.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onDial,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Phone, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Chiama $LABA_PHONE_DISPLAY")
            }
            OutlinedButton(
                onClick = onEmail,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Email, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(LABA_EMAIL)
            }
        }
    }

    DelegaCard()
}

@Composable
private fun DelegaCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Delega (se non puoi presentarti)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf(
                    Icons.Default.Person to "Documento del delegato",
                    Icons.Default.Description to "Delega firmata",
                    Icons.Default.Badge to "Documento studente"
                ).forEach { (icon, text) ->
                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(icon, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(text, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                    }
                }
            }
            Text("Cos'è la delega", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                "Con la delega puoi incaricare un'altra persona a ritirare la pergamena al posto tuo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text("Per procedere servono:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            listOf(
                "Documento del delegato: carta d'identità o patente in corso di validità.",
                "Delega firmata: semplice autorizzazione scritta e firmata dallo studente (usa il modulo standard se disponibile).",
                "Documento studente: copia del documento d'identità del titolare (\"fronte/retro\")."
            ).forEach { item ->
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(item, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                "Senza uno di questi documenti il ritiro non può essere effettuato. La Segreteria potrebbe richiedere una copia da trattenere.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OverviewAppointmentBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Appuntamento obbligatorio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Strettamente necessario",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "per il ritiro della pergamena",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
                )
            }
            Text(
                text = "Il ritiro avviene solo su appuntamento concordato presso la sede di Piazza di Badia a Ripoli 1/A.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InBreveOverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("In breve", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            InBreveStepRow(
                Icons.Default.School,
                "Avvia la richiesta",
                "Dopo aver superato la tesi, avvia la richiesta della pergamena."
            )
            InBreveStepRow(
                Icons.Default.Euro,
                "Bollettino postale",
                "Paga il bollettino (c/c 1016): è il pagamento per avviare la richiesta, non per il ritiro."
            )
            InBreveStepRow(
                Icons.Default.Upload,
                "Consegna in Segreteria",
                "Porta ricevuta del bollettino e modulo di richiesta. La marca da bollo non serve in questa fase."
            )
            InBreveStepRow(
                Icons.Default.Schedule,
                "Ritiro su appuntamento",
                "Contatta la Segreteria per fissare giorno e ora; il giorno del ritiro presenta la marca da bollo da € 16."
            )
        }
    }
}

@Composable
private fun InBreveStepRow(icon: ImageVector, title: String, detail: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TextBlockCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun KpiCard2Line(
    value: String,
    caption: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = contentColor)
            Text(
                caption,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            content()
        }
    }
}

enum class PergamenaPane {
    Overview,
    Pagamento,
    Ritiro
}
