package com.davidbugayov.financeanalyzer

import android.os.Build
import com.davidbugayov.financeanalyzer.analytics.AnalyticsUtils
import com.davidbugayov.financeanalyzer.analytics.AppMetricaAnalyticsAdapter
import com.davidbugayov.financeanalyzer.analytics.CompositeAnalytics
import com.davidbugayov.financeanalyzer.analytics.FirebaseAnalyticsAdapter
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import timber.log.Timber

/**
 * Основной класс приложения для RuStore flavor
 */
class FinanceApp : BaseFinanceApp() {
    // AppMetrica API ключ
    private val appMetricaApiKey = "d4ec51de-47c3-4997-812f-97b9a6663dad"

    // Составной адаптер для объединения всех систем аналитики
    private val compositeAnalytics = CompositeAnalytics()

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    /**
     * Инициализирует компоненты, специфичные для RuStore flavor
     */
    override fun initFlavor() {
        // Логируем событие открытия приложения
        AnalyticsUtils.logAppOpen()
    }

    /**
     * Инициализирует аналитику
     */
    private fun initAnalytics() {
        // Инициализация AppMetrica
        initAppMetrica()

        // Инициализация Firebase
        initFirebase()

        // Устанавливаем составной адаптер аналитики в AnalyticsUtils
        AnalyticsUtils.init(compositeAnalytics)
    }

    /**
     * Инициализирует Яндекс.AppMetrica
     */
    private fun initAppMetrica() {
        try {
            // Настройка AppMetrica
            val config =
                AppMetricaConfig.newConfigBuilder(appMetricaApiKey)
                    .withLogs()
                    .withSessionTimeout(60)
                    .withCrashReporting(true)
                    .build()

            // Активация SDK
            AppMetrica.activate(this, config)

            // Включаем отправку статистики
            AppMetrica.enableActivityAutoTracking(this)

            // Добавляем адаптер в составную аналитику
            compositeAnalytics.addAnalytics(AppMetricaAnalyticsAdapter())

            Timber.d("AppMetrica успешно инициализирована")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка инициализации AppMetrica")
        }
    }

    /**
     * Инициализирует Firebase аналитику и добавляет ее в CompositeAnalytics
     */
    private fun initFirebase() {
        try {
            FirebaseApp.initializeApp(this)
            firebaseAnalytics = Firebase.analytics

            // Создаем адаптер для Firebase Analytics
            val firebaseAdapter = FirebaseAnalyticsAdapter(firebaseAnalytics)

            // Добавляем адаптер в составную аналитику
            compositeAnalytics.addAnalytics(firebaseAdapter)

            // Устанавливаем базовые пользовательские свойства
            firebaseAdapter.setUserProperty("app_version", BuildConfig.VERSION_NAME)
            firebaseAdapter.setUserProperty(
                "build_type",
                if (BuildConfig.DEBUG) "debug" else "release",
            )
            firebaseAdapter.setUserProperty("device_model", Build.MODEL)
            firebaseAdapter.setUserProperty("android_version", Build.VERSION.RELEASE)
            firebaseAdapter.setUserProperty("store", "rustore")

            // Crashlytics отключен для F-Droid совместимости

            Timber.d("Firebase успешно инициализирован")
        } catch (e: Exception) {
            Timber.e(e, "Ошибка инициализации Firebase")
        }
    }

    override fun onCreate() {
        super.onCreate()
    }
}
