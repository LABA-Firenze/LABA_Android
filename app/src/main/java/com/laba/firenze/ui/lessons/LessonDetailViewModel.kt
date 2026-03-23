package com.laba.firenze.ui.lessons

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laba.firenze.data.repository.LessonCalendarRepository
import com.laba.firenze.domain.model.LessonEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class LessonDetailUiState(
    val lesson: LessonEvent? = null,
    val isLoading: Boolean = true,
    val notFound: Boolean = false
)

@HiltViewModel
class LessonDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val lessonCalendarRepository: LessonCalendarRepository
) : ViewModel() {

    private val lessonId = savedStateHandle.get<String>("lessonId") ?: ""

    val uiState: StateFlow<LessonDetailUiState> = lessonCalendarRepository.events
        .map { events ->
            val lesson = events.find { it.id == lessonId }
            LessonDetailUiState(
                lesson = lesson,
                isLoading = events.isEmpty() && lessonId.isNotEmpty(),
                notFound = events.isNotEmpty() && lesson == null && lessonId.isNotEmpty()
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LessonDetailUiState(isLoading = true)
        )

    fun formatTimeRange(start: Date, end: Date): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.ITALIAN)
        return "${timeFormat.format(start)} - ${timeFormat.format(end)}"
    }

    fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ITALIAN)
        return formatter.format(date).replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ITALIAN) else it.toString()
        }
    }
}
