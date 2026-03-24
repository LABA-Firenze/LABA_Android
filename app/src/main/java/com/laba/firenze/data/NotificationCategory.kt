package com.laba.firenze.data

/**
 * Categorie notifiche app (identico a iOS: solo quelle con topic FCM).
 * generali, materiali, assenze.
 */
enum class NotificationCategory(
    val value: String,
    val displayName: String,
    val preferenceKey: String
) {
    GENERAL("generali", "Comunicazioni generali", "notifications.general"),
    MATERIALS("materiali", "Materiali e dispense", "notifications.materials"),
    ABSENCES("assenze", "Assenze e cambi aula", "notifications.absences");

    val topicSuffix: String get() = value
}

