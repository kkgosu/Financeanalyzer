package com.davidbugayov.financeanalyzer.utils

/**
 * Общий интерфейс для планировщика уведомлений, чтобы feature-модули не зависели от реализации.
 */
interface INotificationScheduler {
    /**
     * Включить / отключить ежедневное напоминание и (опционально) задать новое время.
     */
    fun updateTransactionReminder(
        isEnabled: Boolean,
        reminderTime: Time? = null,
    )
}
