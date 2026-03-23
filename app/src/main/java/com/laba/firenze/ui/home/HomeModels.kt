package com.laba.firenze.ui.home

/**
 * Modelli UI condivisi tra HomeScreen e HomeViewModel.
 */
data class LessonUi(
    val title: String,
    val time: String,
    val room: String?,
    val teacher: String?,
    val date: String,
    val isNow: Boolean = false
)

data class YearProgress(
    val year1: Double = 0.0,
    val year1Total: Int = 0,
    val year1Missing: Int = 0,
    val year2: Double = 0.0,
    val year2Total: Int = 0,
    val year2Missing: Int = 0,
    val year3: Double = 0.0,
    val year3Total: Int = 0,
    val year3Missing: Int = 0
) {
    fun getProgressForYear(year: Int): Double {
        return when (year) {
            1 -> year1
            2 -> year2
            3 -> year3
            else -> 0.0
        }
    }

    @Suppress("UNUSED_FUNCTION")
    fun getTotalForYear(year: Int): Int {
        return when (year) {
            1 -> year1Total
            2 -> year2Total
            3 -> year3Total
            else -> 0
        }
    }

    fun getMissingForYear(year: Int): Int {
        return when (year) {
            1 -> year1Missing
            2 -> year2Missing
            3 -> year3Missing
            else -> 0
        }
    }
}
