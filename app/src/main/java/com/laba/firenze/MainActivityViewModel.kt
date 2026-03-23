package com.laba.firenze

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    val appearancePreferences: com.laba.firenze.data.local.AppearancePreferences
) : ViewModel() {
    
    val accentChoice: StateFlow<String> = appearancePreferences.accentChoice
    val themePreference: kotlinx.coroutines.flow.Flow<String> = appearancePreferences.themePreference
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    /** True durante il refresh del token (per mostrare "Aggiornamento accesso" come su iOS). */
    val isRefreshingSession: StateFlow<Boolean> = sessionRepository.isRefreshingSession
    
    /** Numero esami prenotabili (badge "!" sul tab Esami). Identico a iOS bookableExamsCount. */
    val bookableExamsCount: StateFlow<Int> = sessionRepository.exams
        .map { list -> list.count { it.richiedibile } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    /** Numero seminari prenotabili (badge numerico sul tab Attività). Identico a iOS bookableSeminarsCount. */
    val bookableSeminarsCount: StateFlow<Int> = sessionRepository.seminars
        .map { list -> list.count { it.richiedibile } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    /** Deep link laba://lesson/{lessonId} (identico a iOS handleDeepLink). */
    private val _pendingDeepLink = MutableStateFlow<String?>(null)
    val pendingDeepLink: StateFlow<String?> = _pendingDeepLink.asStateFlow()
    
    fun setPendingDeepLink(lessonId: String?) {
        _pendingDeepLink.value = lessonId
    }
    
    fun clearPendingDeepLink() {
        _pendingDeepLink.value = null
    }
    
    /** Tap su notifica FCM → apri dettaglio singola notifica (title/body dall'intent). */
    data class NotificationTapPayload(val title: String, val body: String)
    private val _pendingNotificationTap = MutableStateFlow<NotificationTapPayload?>(null)
    val pendingNotificationTap: StateFlow<NotificationTapPayload?> = _pendingNotificationTap.asStateFlow()
    
    fun setPendingNotificationTap(title: String, body: String) {
        _pendingNotificationTap.value = NotificationTapPayload(title = title, body = body)
    }
    
    fun clearPendingNotificationTap() {
        _pendingNotificationTap.value = null
    }
    
    /** Deep link documento da push (tipo 0=Programmi, tipo 1=Dispense + oid). ensureTabInBar + apri doc. */
    data class DocumentDeepLinkPayload(val tipo: Int, val oid: String)
    private val _pendingDocumentDeepLink = MutableStateFlow<DocumentDeepLinkPayload?>(null)
    val pendingDocumentDeepLink: StateFlow<DocumentDeepLinkPayload?> = _pendingDocumentDeepLink.asStateFlow()
    
    fun setPendingDocumentDeepLink(tipo: Int, oid: String) {
        _pendingDocumentDeepLink.value = DocumentDeepLinkPayload(tipo = tipo, oid = oid)
    }
    
    fun clearPendingDocumentDeepLink() {
        _pendingDocumentDeepLink.value = null
    }
    
    init {
        // Monitor login state (mantieni isLoading attivo se i dati stanno caricando)
        viewModelScope.launch {
            sessionRepository.tokenManager.isLoggedIn.collect { isLoggedIn ->
                if (isLoggedIn) {
                    // Se è loggato, mantieni isLoading true per mostrare lo splash
                    _authState.value = _authState.value.copy(isLoggedIn = true)
                } else {
                    // Se non è loggato, sicuramente non carica
                    _authState.value = _authState.value.copy(
                        isLoggedIn = false,
                        isLoading = false
                    )
                }
            }
        }
        
        // Monitor loading state - quando finisce di caricare, aggiorna isLoading
        viewModelScope.launch {
            sessionRepository.isLoading.collect { isLoading ->
                if (!isLoading && _authState.value.isLoggedIn) {
                    // I dati sono stati caricati e l'utente è loggato
                    _authState.value = _authState.value.copy(isLoading = false)
                }
            }
        }
        
        // Silent login all'avvio (identico a iOS cold start: restore + forceLogoutIfExpired)
        viewModelScope.launch {
            try {
                println("🔐 MainActivityViewModel: Attempting silent login on startup")
                sessionRepository.restoreSessionStrong(force = false)
                sessionRepository.forceLogoutIfExpired()
                if (sessionRepository.isLoggedIn()) {
                    println("🔐 MainActivityViewModel: Silent login successful")
                    if (!sessionRepository.isLoading.value) {
                        _authState.value = _authState.value.copy(isLoading = false)
                    }
                } else {
                    println("🔐 MainActivityViewModel: Silent login failed, showing login UI")
                }
            } catch (e: Exception) {
                println("🔐 MainActivityViewModel: Silent login exception: ${e.message}")
            }
        }
    }
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            try {
                val success = sessionRepository.login(username, password)
                if (!success) {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "Credenziali non valide"
                    )
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Errore durante il login"
                )
            }
        }
    }
    
    fun clearError() {
        _authState.value = _authState.value.copy(errorMessage = null)
    }
    
    /** Skip primo ON_RESUME: l'init gestisce già cold start (restore + forceLogoutIfExpired). */
    private var hasHandledFirstResume = false
    /** Debounce: evita loop di "Aggiornamento in corso" quando ON_RESUME fire multiple volte. */
    private var lastAppResumedAt = 0L
    private val appResumedDebounceMs = 5000L
    
    /**
     * Chiamare quando l'app torna in foreground (identico a iOS handleSceneBecameActive).
     */
    fun onAppResumed() {
        if (!hasHandledFirstResume) {
            hasHandledFirstResume = true
            return // Cold start gestito dall'init
        }
        val now = System.currentTimeMillis()
        if (now - lastAppResumedAt < appResumedDebounceMs) return
        lastAppResumedAt = now
        
        viewModelScope.launch {
            try {
                sessionRepository.handleAppResumed()
            } catch (e: Exception) {
                println("MainActivityViewModel: onAppResumed error: ${e.message}")
            }
        }
    }
    
    /**
     * Forza il restore della sessione (utile per "Riprova adesso")
     */
    fun forceRestoreSession() {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            
            try {
                val success = sessionRepository.restoreSessionStrong(force = true)
                if (!success) {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "Impossibile ripristinare la sessione"
                    )
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Errore durante il ripristino"
                )
            }
        }
    }
}

data class AuthState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
