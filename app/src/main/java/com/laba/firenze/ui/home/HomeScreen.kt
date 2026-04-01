package com.laba.firenze.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import android.content.Context
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.laba.firenze.domain.model.Esame
import com.laba.firenze.data.repository.SessionRepository
import com.laba.firenze.ui.home.HomeViewModel
import android.content.SharedPreferences
import kotlin.math.*
import kotlin.random.Random
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val sharedPrefs = remember { 
        context.getSharedPreferences("LABA_PREFS", Context.MODE_PRIVATE)
    }
    
    // Controlla se le funzionalità sono abilitate
    val timetableEnabled = remember { 
        sharedPrefs.getBoolean("laba.timetable.enabled", false)
    }
    @Suppress("UNUSED_VARIABLE")
    val achievementsEnabled = remember { 
        sharedPrefs.getBoolean("laba.achievements.enabled", false)
    }
    
    LaunchedEffect(Unit) { 
        viewModel.refreshOnAppear() 
    }

    var userInitiatedRefresh by remember { mutableStateOf(false) }
    val view = LocalView.current
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            if (userInitiatedRefresh) {
                view.performHapticFeedback(HapticFeedbackConstantsCompat.CONFIRM)
            }
            userInitiatedRefresh = false
        }
    }
    val isRefreshing = userInitiatedRefresh && uiState.isLoading
    val onRefresh = {
        userInitiatedRefresh = true
        viewModel.performRefresh()
    }
    val pullRefreshState = rememberPullRefreshState(isRefreshing, onRefresh)
    
    val sectionOrder by viewModel.sectionOrder.collectAsStateWithLifecycle()
    val displaySections = remember(sectionOrder) { normalizeHomeSectionOrder(sectionOrder) }
    val profile = viewModel.getUserProfile()
    val exams = uiState.bookedExams
    val allExams = viewModel.getAllExams()
    val shouldShowBooked = viewModel.shouldShowBookedExams(profile, allExams)
    
    Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp, 
            end = 20.dp, 
            top = 16.dp,
            bottom = 140.dp
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        displaySections.forEach { sectionId ->
            when (sectionId) {
                "hero_kpi" -> item {
                    val dataAppeared = !uiState.isLoading
                    val heroInfo = viewModel.getHeroInfo()
                    UnifiedHeroAndKpiSection(
                        heroInfo = heroInfo,
                        statusPills = uiState.statusPills,
                        isGraduated = uiState.isGraduated,
                        heroGraduatePhrase = viewModel.getHeroPhraseForGraduate(),
                        selectedPattern = viewModel.getHeroPattern(),
                        heroNotificationPreview = uiState.heroNotificationPreview,
                        onNavigateToInbox = { navController.navigate("inbox") },
                        passedExams = uiState.passedExamsCount,
                        missingExams = uiState.missingExamsCount,
                        cfaEarned = uiState.cfaEarned,
                        totalExams = uiState.totalExamsCount,
                        dataAppeared = dataAppeared
                    )
                }
                "hero" -> item {
                    val heroInfo = viewModel.getHeroInfo()
                    HeroSection(
                        heroInfo = heroInfo,
                        statusPills = uiState.statusPills,
                        isGraduated = uiState.isGraduated,
                        heroGraduatePhrase = viewModel.getHeroPhraseForGraduate(),
                        selectedPattern = viewModel.getHeroPattern(),
                        heroNotificationPreview = uiState.heroNotificationPreview,
                        onNavigateToInbox = { navController.navigate("inbox") }
                    )
                }
                "kpi" -> item {
                    val dataAppeared = !uiState.isLoading
                    KpiCardsSection(
                        passedExams = uiState.passedExamsCount,
                        missingExams = uiState.missingExamsCount,
                        cfaEarned = uiState.cfaEarned,
                        totalExams = uiState.totalExamsCount,
                        isGraduated = uiState.isGraduated,
                        dataAppeared = dataAppeared
                    )
                }
                "progress" -> item {
                    YearProgressAndAverageSection(
                        yearProgress = uiState.yearProgress,
                        careerAverage = uiState.careerAverage,
                        isBiennio = isBiennioLevel(profile),
                        onNavigateToGrades = { navController.navigate("grades/trend") }
                    )
                }
                "exams" -> {
                    // Esami in Home solo nei periodi prefissati d'esame (come iOS)
                    if (viewModel.isTodayInExamSession() && !uiState.isGraduated && profile?.currentYear != null && shouldShowBooked && exams.isNotEmpty()) {
                        item {
                            BookedExamsSection(
                                exams = exams.take(3),
                                onNavigateToAll = { navController.navigate("esami-prenotati") }
                            )
                        }
                    }
                }
                "lessons" -> {
                    if (timetableEnabled) {
                        item {
                            LessonsTodayCard(lessons = uiState.lessonsToday)
                        }
                        item {
                            TextButton(
                                onClick = { navController.navigate("full_timetable") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Visualizza orario completo")
                            }
                        }
                    }
                }
                "quickActions" -> {
                    item {
                        PerTeSection(
                            viewModel = viewModel,
                            onNavigate = { route ->
                                when (route) {
                                    "Valutazione finale" -> navController.navigate("calcola-voto-laurea")
                                    "Simula la tua media" -> navController.navigate("simula-media")
                                }
                            }
                        )
                    }
                    item {
                        ServiziSection(
                            onNavigate = { route ->
                                when (route) {
                                    "Service LABA" -> navController.navigate("strumentazione")
                                    "Prenota le aule" -> navController.navigate("prenotazione-aule")
                                    "Biblioteca" -> navController.navigate("biblioteca")
                                }
                            },
                            showGestioneServizi = false
                        )
                    }
                }
            }
        }
    }
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

/** Unisce "hero" + "kpi" adiacenti come su iOS (`unifiedHeroAndKpiSection`). */
private fun normalizeHomeSectionOrder(order: List<String>): List<String> {
    val out = ArrayList<String>(order.size)
    var i = 0
    while (i < order.size) {
        if (order[i] == "hero" && i + 1 < order.size && order[i + 1] == "kpi") {
            out.add("hero_kpi")
            i += 2
        } else {
            out.add(order[i])
            i++
        }
    }
    return out
}

/** Testo KPI laureato: come iOS (`HomeView`); italiano fisso come il resto della Home (non `stringResource` / locale sistema). */
private const val HOME_GRADUATED_KPI_TEXT = "Hai terminato gli studi 🔥"

// MARK: - Hero + KPI unificato (iOS: unifiedHeroAndKpiSection)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnifiedHeroAndKpiSection(
    heroInfo: HeroInfo,
    statusPills: List<String>,
    isGraduated: Boolean,
    heroGraduatePhrase: String,
    selectedPattern: String,
    heroNotificationPreview: String?,
    onNavigateToInbox: () -> Unit,
    passedExams: Int,
    missingExams: Int,
    cfaEarned: Int,
    totalExams: Int,
    dataAppeared: Boolean
) {
    val view = LocalView.current
    val infiniteTransition = rememberInfiniteTransition(label = "unified_hero_kpi")
    val animationPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "unified_hero_kpi_phase"
    )
    val scale by animateFloatAsState(
        targetValue = if (dataAppeared) 1f else 0.98f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f),
        label = "unified_hero_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (dataAppeared) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f),
        label = "unified_hero_alpha"
    )
    val shape = RoundedCornerShape(16.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .graphicsLayer { this.alpha = alpha }
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.12f)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                shape = shape
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Alto chiaro: saluto + pill (come iOS unifiedHeroIntroLightTop)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 22.dp)
            ) {
                Text(
                    text = "Ciao, ${heroInfo.displayName}! 👋",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                StatusPillsRow(
                    heroInfo = heroInfo,
                    pills = statusPills,
                    isGraduated = isGraduated,
                    heroGraduatePhrase = heroGraduatePhrase,
                    lightBackground = true
                )
                heroNotificationPreview?.let { msg ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateToInbox),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)
            )
            // Basso: gradiente + pattern KPI (come iOS unifiedHeroKpiAccentBottom).
            // Senza altezza minima, `Canvas(fillMaxSize())` può misurarsi ~0 px → onde “schiacciate” in una linea.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp)
                    .clip(
                        RoundedCornerShape(
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        )
                    )
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.72f)
                            )
                        )
                    )
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val time = animationPhase * 60f
                    when (selectedPattern) {
                        "wave" -> drawWavePatternHero(size, time)
                        "dots" -> drawDotsPatternHero(size, time)
                        "grid" -> drawGridPatternHero(size, time)
                        "particles" -> drawParticlesPatternHero(size, time)
                        "circles" -> drawCirclesPatternHero(size, time)
                        "rays" -> drawRaysPatternHero(size, time)
                        "ripple" -> drawRipplePatternHero(size, time)
                        else -> drawWavePatternHero(size, time)
                    }
                }
                if (isGraduated) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .matchParentSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = HOME_GRADUATED_KPI_TEXT,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        KpiGradientColumn(
                            title = "Esami\nsostenuti",
                            value = passedExams.toString(),
                            isComplete = false,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstantsCompat.LONG_PRESS)
                                }
                        )
                        KpiGradientColumn(
                            title = "Esami\nmancanti",
                            value = missingExams.toString(),
                            isComplete = missingExams == 0 && totalExams > 0,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstantsCompat.LONG_PRESS)
                                }
                        )
                        KpiGradientColumn(
                            title = "CFA \nacquisiti",
                            value = cfaEarned.toString(),
                            isComplete = false,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstantsCompat.LONG_PRESS)
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiGradientColumn(
    title: String,
    value: String,
    isComplete: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.height(96.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isComplete) {
            Icon(
                imageVector = Icons.Rounded.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = Color.White
            )
            Text(
                text = "Hai sostenuto tutti gli esami!",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.95f),
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.88f),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

// MARK: - Hero Section (standalone: vecchio hero full-bleed gradient)
@Composable
private fun HeroSection(
    heroInfo: HeroInfo,
    statusPills: List<String>,
    isGraduated: Boolean,
    heroGraduatePhrase: String = "ma perché usi ancora l'app?",
    selectedPattern: String = "wave",
    heroNotificationPreview: String? = null,
    onNavigateToInbox: () -> Unit = {}
) {
    val density = LocalDensity.current
    val infiniteTransition = rememberInfiniteTransition(label = "hero_animation")
    val animationPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hero_phase"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                )
            )
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            )
    ) {
        // Animated pattern overlay based on selection
        with(density) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                // Convert animationPhase (0-1) to time scale (60s duration)
                val time = animationPhase * 60f
                
                when (selectedPattern) {
                    "wave" -> drawWavePatternHero(size, time)
                    "dots" -> drawDotsPatternHero(size, time)
                    "grid" -> drawGridPatternHero(size, time)
                    "particles" -> drawParticlesPatternHero(size, time)
                    "circles" -> drawCirclesPatternHero(size, time)
                    "rays" -> drawRaysPatternHero(size, time)
                    "ripple" -> drawRipplePatternHero(size, time)
                    else -> drawWavePatternHero(size, time)
                }
            }
        }
        
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Ciao, ${heroInfo.displayName}! 👋",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            StatusPillsRow(
                heroInfo = heroInfo,
                pills = statusPills,
                isGraduated = isGraduated,
                heroGraduatePhrase = heroGraduatePhrase
            )
            
            // Messaggio notifica non letta (reformatMessaggioHome - identico a iOS 2.8.2)
            heroNotificationPreview?.let { msg ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToInbox),
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.95f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPillsRow(
    heroInfo: HeroInfo,
    @Suppress("UNUSED_PARAMETER") pills: List<String>,
    isGraduated: Boolean,
    heroGraduatePhrase: String = "ma perché usi ancora l'app?",
    lightBackground: Boolean = false
) {
    val secondary = if (lightBackground) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        Color.White.copy(alpha = 0.95f)
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isGraduated) {
            // Se laureato: pillola + frase ciclica (identico a iOS heroPhraseForGraduate)
            Pill("Laureato", lightBackground = lightBackground)
            Text(
                text = heroGraduatePhrase,
                style = MaterialTheme.typography.bodyMedium,
                color = secondary
            )
        } else {
            // Se non laureato: mostra anno di corso e corso compatto
            heroInfo.studyYear?.let { year ->
                Pill(getItalianOrdinalYear(year), lightBackground = lightBackground)
            }
            
            // Corso compatto + A.A. (se disponibile)
            val courseDisplay = if (heroInfo.academicYear.isNotEmpty()) {
                "${heroInfo.courseName} ${heroInfo.academicYear}"
            } else {
                heroInfo.courseName
            }
            
            Text(
                text = courseDisplay,
                style = MaterialTheme.typography.bodyMedium,
                color = secondary
            )
        }
    }
}

private fun getItalianOrdinalYear(year: Int): String {
    return when (year) {
        1 -> "1° anno"
        2 -> "2° anno"
        3 -> "3° anno"
        4 -> "4° anno"
        5 -> "5° anno"
        else -> "$year° anno" // Fuoricorso
    }
}

@Composable
private fun Pill(text: String, lightBackground: Boolean = false) {
    val bg = if (lightBackground) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
    }
    val fg = if (lightBackground) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.White
    }
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        style = MaterialTheme.typography.labelMedium,
        color = fg,
        fontWeight = FontWeight.Medium
    )
}

// MARK: - KPI Cards Section
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KpiCardsSection(
    passedExams: Int,
    missingExams: Int,
    cfaEarned: Int,
    totalExams: Int,
    isGraduated: Boolean,
    dataAppeared: Boolean = true
) {
    val view = LocalView.current
    val scale by animateFloatAsState(
        targetValue = if (dataAppeared) 1f else 0.98f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f),
        label = "kpi_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (dataAppeared) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f),
        label = "kpi_alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .graphicsLayer { this.alpha = alpha },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isGraduated) {
            // Allineato a iOS Home: striscia unica al posto delle 3 KPI (graduatedKpiUnifiedFullWidthStrip)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.72f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .defaultMinSize(minHeight = 61.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = HOME_GRADUATED_KPI_TEXT,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Esami sostenuti (haptic on tap)
                KpiCard(
                    title = "Esami\nsostenuti",
                    value = passedExams.toString(),
                    emphasizeGlow = false,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstantsCompat.LONG_PRESS)
                        }
                )

                // Esami mancanti (haptic on tap)
                KpiCard(
                    title = "Esami\nmancanti",
                    value = missingExams.toString(),
                    emphasizeGlow = true,
                    isComplete = missingExams == 0 && totalExams > 0,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstantsCompat.LONG_PRESS)
                        }
                )

                // CFA acquisiti (haptic on tap)
                KpiCard(
                    title = "CFA \nacquisiti",
                    value = cfaEarned.toString(),
                    emphasizeGlow = false,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstantsCompat.LONG_PRESS)
                        }
                )
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    @Suppress("UNUSED_PARAMETER") emphasizeGlow: Boolean, // Non utilizzato ma mantenuto per coerenza API
    isComplete: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.height(96.dp)
    ) {
        // Main card con depth
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isComplete) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isComplete) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Hai sostenuto tutti gli esami!",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

/**
 * Determina se lo studente è del biennio: solo 2 anni, nascondere 3° in home/corsi/esami.
 * Allineato a iOS SessionViewModel.isBiennio (pianoStudi + corsi noti: Interior, Cinema e Audiovisivi).
 */
private fun isBiennioLevel(profile: com.laba.firenze.domain.model.StudentProfile?): Boolean {
    if (profile == null) return false
    val ps = profile.pianoStudi?.lowercase() ?: ""
    val matricola = profile.matricola?.lowercase() ?: ""
    if (ps.contains("biennio") || ps.contains("ii livello") || ps.contains("2° livello") || ps.contains("secondo livello")) return true
    if (ps.contains("interior") || ps.contains("cinema") || ps.contains("audiovisiv")) return true
    if (matricola.contains("biennio") && !matricola.contains("triennio")) return true
    return false
}

// MARK: - Year Progress and Average Section
@Composable
private fun YearProgressAndAverageSection(
    yearProgress: YearProgress?,
    careerAverage: Double?,
    isBiennio: Boolean,
    onNavigateToGrades: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Come stai andando?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Year progress (1st, 2nd, 3rd year - solo 1° e 2° per biennio)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val maxYear = if (isBiennio) 2 else 3
                for (year in 1..maxYear) {
                    YearProgressCard(
                        year = year,
                        progress = yearProgress?.getProgressForYear(year) ?: 0.0,
                        missingCount = yearProgress?.getMissingForYear(year) ?: 0,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
            
            // Career Average (tappable)
            CareerAverageCard(
                average = careerAverage,
                onClick = onNavigateToGrades
            )
        }
    }
}

@Composable
private fun YearProgressCard(
    year: Int,
    progress: Double,
    missingCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Esami ${getItalianOrdinalYear(year)}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        // Thin progress bar
        ThinProgressBar(progress = progress)
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (progress >= 1.0) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Completati",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = "$missingCount mancanti",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ThinProgressBar(progress: Double) {
    val clampedProgress = max(0.0, min(1.0, progress))
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(clampedProgress.toFloat())
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                        )
                    )
                )
        )
    }
}

@Composable
private fun CareerAverageCard(
    average: Double?,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "La tua media",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = average?.let { String.format(Locale.getDefault(), "%.2f", it) } ?: "—",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Progress bar for average
        val averageProgress = average?.let { max(0.0, min(1.0, it / 30.0)) } ?: 0.0
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(averageProgress.toFloat())
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                            )
                        )
                    )
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Dettaglio media",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// MARK: - Lessons Today Card (sempre presente, come iOS)
@Composable
private fun LessonsTodayCard(lessons: List<LessonUi>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Lezioni di oggi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (lessons.isEmpty()) {
                // Nessuna lezione - mostra messaggio come iOS
                Text(
                    text = "Oggi",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Nessuna lezione oggi.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
                Text(
                    text = "Nessuna lezione in calendario nei prossimi giorni.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Ci sono lezioni - mostra elenco
                Text(
                    text = "Oggi",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                lessons.forEachIndexed { index, lesson ->
                    LessonRow(lesson = lesson)
                    if (index < lessons.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 2.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    }
                }
                
                // Se ci sono solo poche lezioni, mostra anche domani (come iOS)
                if (lessons.size <= 2) {
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                    Text(
                        text = "Domani",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Nessuna lezione in calendario.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonRow(lesson: LessonUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Date badge - GIORNO non orario!
        DayBadge(date = lesson.date)
        
        // Time column
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = lesson.time,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
        }
        
        // Course info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = lesson.title,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                lesson.room?.let { room: String ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = room,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                
                lesson.teacher?.let { teacher ->
                    if (lesson.room != null) {
                        Text("–", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = teacher,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
        
        if (lesson.isNow) {
            DayBadge(date = "", text = "ORA", isNow = true)
        }
    }
}

@Composable
private fun DayBadge(date: String, text: String = "", isNow: Boolean = false) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                if (isNow) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isNow) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Default
            )
        } else {
            // Mostra il GIORNO della settimana
            val dayOfWeek = try {
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dateObj = formatter.parse(date)
                val dayFormatter = SimpleDateFormat("EEE", Locale("it", "IT"))
                dayFormatter.format(dateObj ?: Date()).uppercase()
            } catch (e: Exception) {
                "OGG"
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(-2.dp)
            ) {
                Text(
                    text = dayOfWeek,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 9.sp
                )
                Text(
                    text = try {
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val dateObj = formatter.parse(date)
                        val dayFormatter = SimpleDateFormat("dd", Locale.getDefault())
                        dayFormatter.format(dateObj ?: Date())
                    } catch (e: Exception) {
                        "15"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// MARK: - Per Te Section (come iOS - solo Voto di laurea e Simula media)
@Composable
private fun PerTeSection(
    viewModel: HomeViewModel,
    onNavigate: (String) -> Unit
) {
    val profile = viewModel.getUserProfile()
    @Suppress("UNUSED_VARIABLE")
    val allExams = viewModel.getAllExams()
    val isGraduated = profile?.status?.lowercase()?.contains("laureat") == true
    val hasCompletedAllExams = viewModel.hasCompletedAllGraduationExams()
    val currentYear = profile?.currentYear?.toIntOrNull()
    val canAccessGraduationGrade = isGraduated || hasCompletedAllExams || currentYear == 3
    
    var showGraduationGradeAlert by remember { mutableStateOf(false) }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text("Per te", style = MaterialTheme.typography.titleMedium)
        }
        
        // Voto di laurea (con accesso condizionale)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { 
                    if (canAccessGraduationGrade) {
                        onNavigate("Valutazione finale")
                    } else {
                        showGraduationGradeAlert = true
                    }
                },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = if (canAccessGraduationGrade) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Valutazione finale",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (canAccessGraduationGrade) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Simula la tua media
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigate("Simula la tua media") },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Simula la tua media",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    // Alert per sezione non disponibile
    if (showGraduationGradeAlert) {
        AlertDialog(
            onDismissRequest = { 
                @Suppress("UNUSED_VALUE")
                showGraduationGradeAlert = false 
            },
            title = { Text("Sezione non disponibile") },
            text = {
                Text(
                    "Il calcolo del voto di laurea sarà disponibile appena avrai completato tutti gli esami del tuo piano di studi. Continua con gli ultimi passi e torna qui per prepararti alla discussione!"
                )
            },
            confirmButton = {
                TextButton(onClick = { showGraduationGradeAlert = false }) {
                    Text("OK")
                }
            }
        )
    }
}

// MARK: - Servizi Section (separata, come iOS)
@Composable
private fun ServiziSection(
    onNavigate: (String) -> Unit,
    @Suppress("UNUSED_PARAMETER") showGestioneServizi: Boolean = true
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Default.TabletMac,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text("Servizi", style = MaterialTheme.typography.titleMedium)
        }
        
        // Service LABA
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigate("Service LABA") },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Service LABA",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Prenotazione Aule
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigate("Prenota le aule") },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MeetingRoom,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Prenota le aule",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Biblioteca
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigate("Biblioteca") },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Biblioteca",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Servizi (gestione funzionalità) - solo se showGestioneServizi è true
        if (showGestioneServizi) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("Servizi") },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Gestione servizi",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// MARK: - Helper Functions

// MARK: - Hero Wave Pattern Drawing (animated like iOS)
private fun DrawScope.drawWavePatternHero(size: Size, time: Float) {
    val color = Color.White.copy(alpha = 0.8f)
    val waveCount = 8
    val spacing = size.height / waveCount
    val t = time * 0.6f
    
    for (i in 0 until waveCount) {
        val yPos = i * spacing
        val wavePhase = sin((i * 0.5 + t * 1.2)) * 0.3f
        val amplitude: Float = 8f + (i % 3) * 2f
        
        val wavePath = Path()
        
        for (x in 0..size.width.toInt() step 2) {
            val normalizedX = x.toFloat() / size.width
            val wave = sin((normalizedX * 6.0 + i * 0.7 + t * 1.5).toDouble()).toFloat() * amplitude
            val y = yPos + wave + (wavePhase.toFloat() * 3)
            
            if (x == 0) {
                wavePath.moveTo(x.toFloat(), y)
            } else {
                wavePath.lineTo(x.toFloat(), y)
            }
        }
        
        val opacity = 0.4f + (i % 2) * 0.2f
        drawPath(
            path = wavePath,
            color = color.copy(alpha = opacity),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
        )
    }
}

// MARK: - Additional Hero Patterns

private fun DrawScope.drawDotsPatternHero(size: Size, time: Float) {
    val color = Color.White.copy(alpha = 0.8f)
    val step = 28f
    val radius = 3f
    val t = time * 0.6f
    
    var y = -step
    while (y <= size.height + step) {
        var x = -step
        while (x <= size.width + step) {
            val seed = (x * 13 + y * 7).toInt()
            val dx = sin((x + y) / 140f + t * (1.2f + 0.17f * sin(seed.toFloat()))) * 10f * (0.8f + 0.3f * cos(seed.toFloat()))
            val dy = cos((x - y) / 120f + t * (1.3f + 0.23f * cos((seed + 99).toFloat()))) * 10f * (0.8f + 0.3f * sin((seed + 42).toFloat()))
            
            drawCircle(
                color = color,
                radius = radius,
                center = Offset(x + dx, y + dy)
            )
            x += step
        }
        y += step
    }
}

private fun DrawScope.drawGridPatternHero(size: Size, time: Float) {
    val color = Color.White.copy(alpha = 0.8f)
    val t = time * 0.4f
    val spacing = 40f
    
    // Vertical lines
    for (x in 0..size.width.toInt() step spacing.toInt()) {
        val offset = sin(t * 0.8f + x * 0.01f) * 20f
        drawLine(
            color = color.copy(alpha = 0.2f),
            start = Offset(x.toFloat(), -offset),
            end = Offset(x.toFloat(), size.height + offset),
            strokeWidth = 1.5f
        )
    }
    
    // Horizontal lines
    for (y in 0..size.height.toInt() step spacing.toInt()) {
        val offset = cos(t * 0.6f + y * 0.01f) * 20f
        drawLine(
            color = color.copy(alpha = 0.2f),
            start = Offset(-offset, y.toFloat()),
            end = Offset(size.width + offset, y.toFloat()),
            strokeWidth = 1.5f
        )
    }
}

private fun DrawScope.drawParticlesPatternHero(size: Size, time: Float) {
    val color = Color.White.copy(alpha = 0.8f)
    val t = time * 0.5f
    val particleCount = 50
    
    for (i in 0 until particleCount) {
        val seed = i * 123.456f
        val sx = (seed * 7.891) % size.width
        val sy = (seed * 4.567) % size.height
        val x = ((sx + sin(t * 0.8f + seed * 0.1f) * 40f) % size.width).toFloat()
        val y = ((sy + cos(t * 0.6f + seed * 0.15f) * 40f) % size.height).toFloat()
        val size = 3f + (i % 3) * 2f
        val opacity = 0.4f + (i % 3) * 0.2f
        
        drawCircle(
            color = color.copy(alpha = opacity),
            radius = size,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawCirclesPatternHero(size: Size, time: Float) {
    val color = Color.White.copy(alpha = 0.8f)
    val t = time * 0.4f
    val circleCount = 20
    
    for (i in 0 until circleCount) {
        val seed = i * 45.678f
        val cx = ((seed * 3.141) % size.width).toFloat()
        val cy = ((seed * 2.718) % size.height).toFloat()
        val pulse = sin(t + i * 0.5f)
        val radius = 15f + pulse * 25f
        
        drawCircle(
            color = color.copy(alpha = 0.7f),
            radius = radius,
            center = Offset(cx, cy),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
        )
    }
}

private fun DrawScope.drawRaysPatternHero(size: Size, time: Float) {
    val color = Color.White.copy(alpha = 0.8f)
    val t = time * 0.3f
    
    val rayCount = 12
    for (i in 0 until rayCount) {
        val angle = (i * 2f * PI.toFloat() / rayCount) + t
        val length = size.height.coerceAtMost(size.width)
        
        val startX = size.width / 2f
        val startY = size.height / 2f
        val endX = startX + cos(angle) * length
        val endY = startY + sin(angle) * length
        
        drawLine(
            color = color.copy(alpha = 0.7f),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 1.5f
        )
    }
}

private fun DrawScope.drawRipplePatternHero(size: Size, time: Float) {
    val color = Color.White.copy(alpha = 0.8f)
    val t = time * 0.5f
    
    val rippleCount = 5
    for (i in 0 until rippleCount) {
        val phase = (t + i * 0.5f) % (2 * PI.toFloat())
        val radius = phase * 30f
        
        val centers = listOf(
            Offset(size.width * 0.3f, size.height * 0.3f),
            Offset(size.width * 0.7f, size.height * 0.7f),
            Offset(size.width * 0.5f, size.height * 0.2f)
        )
        
        centers.forEach { center ->
            if (phase < 2 * PI.toFloat()) {
                drawCircle(
                    color = color.copy(alpha = 0.7f),
                    radius = radius,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                )
            }
        }
    }
}

// MARK: - Booked Exams Section (identica a iOS)
@Composable
private fun BookedExamsSection(
    exams: List<Esame>,
    onNavigateToAll: () -> Unit
) {
    val red = Color(0xFFE53935) // Rosso come originale
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header rosso pastello
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = red
                )
                Text(
                    text = "Prossimi esami",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = red
                )
            }
            
            // Lista esami (massimo 3)
            exams.forEachIndexed { index, exam ->
                BookedExamRow(exam = exam, number = index + 1)
                if (index < exams.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                }
            }
            
            // Pulsante per vedere tutti gli esami prenotati
            TextButton(
                onClick = onNavigateToAll,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = red,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Vedi esami prenotati",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = red
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = red.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BookedExamRow(
    exam: Esame,
    number: Int
) {
    val red = Color(0xFFE53935) // Rosso come originale
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Numero in cerchio rosso
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(red),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
        
        // Info esame
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = prettifyTitle(exam.corso),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            // Data esame (sostenutoIl o dataRichiesta) - formattazione come iOS
            val examDateStr = formatExamDateForDisplay(exam.data ?: exam.dataRichiesta)
            if (examDateStr != null) {
                Text(
                    text = examDateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            exam.docente?.let { docente ->
                if (docente.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = surnamesOnly(docente),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Helper: formatta il titolo in proper case (identico a iOS prettifyTitle)
 */
private fun prettifyTitle(title: String): String {
    return title.replace("_", " ")
        .split(" ")
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase() else it.toString() 
            }
        }
}

/**
 * Formatta la data esame per display (identico a iOS - formato "d MMM" o "EEE d MMM" in italiano).
 */
private fun formatExamDateForDisplay(dateStr: String?): String? {
    if (dateStr.isNullOrBlank()) return null
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        inputFormat.timeZone = java.util.TimeZone.getTimeZone("Europe/Rome")
        val parsed = inputFormat.parse(dateStr) ?: return null
        val outputFormat = SimpleDateFormat("d MMM", Locale("it", "IT"))
        outputFormat.timeZone = java.util.TimeZone.getTimeZone("Europe/Rome")
        outputFormat.format(parsed)
    } catch (e: Exception) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val parsed = inputFormat.parse(dateStr) ?: return null
            val outputFormat = SimpleDateFormat("d MMM", Locale("it", "IT"))
            outputFormat.format(parsed)
        } catch (e2: Exception) {
            null
        }
    }
}

/**
 * Helper: estrae solo i cognomi dal nome del docente (identico a iOS surnamesOnly)
 */
private fun surnamesOnly(docente: String): String {
    val parts = docente.split("/").firstOrNull()?.trim() ?: docente
    val components = parts.split(" ").filter { it.isNotEmpty() }
    return when {
        components.size >= 2 -> components.drop(1).joinToString(" ") // Tutti tranne il primo
        else -> parts
    }
}

// MARK: - Animated Glow Background
@Composable
@Suppress("UNUSED_FUNCTION")
private fun AnimatedGlowBackground(
    modifier: Modifier = Modifier,
    isActive: Boolean,
    phase: Float
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        if (isActive) {
            drawGlowGradient(size, phase, primaryColor)
        }
    }
}

private fun DrawScope.drawGlowGradient(size: Size, phase: Float, primaryColor: Color) {
    val spotsCount = 6
    val seeds = listOf(123f, 456f, 789f, 321f, 654f, 987f)
    
    for (i in 0 until spotsCount) {
        val sx = seeds[i % seeds.size]
        val sy = seeds[(i + 1) % seeds.size]
        val px = 0.5f + 0.42f * sin((phase * 0.18f) + sx / 37f + i)
        val py = 0.5f + 0.42f * cos((phase * 0.15f) + sy / 41f + i)
        
        val baseRadius = min(size.width, size.height) * (0.20f + 0.10f * (i % 3))
        val radius = baseRadius * (0.92f + 0.55f)
        
        val center = Offset(px * size.width, py * size.height)
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.85f),
                    primaryColor.copy(alpha = 0.35f),
                    primaryColor.copy(alpha = 0f)
                ),
                center = center,
                radius = radius
            ),
            center = center,
            radius = radius
        )
    }
}
