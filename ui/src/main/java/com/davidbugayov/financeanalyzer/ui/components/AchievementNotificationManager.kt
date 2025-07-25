package com.davidbugayov.financeanalyzer.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.domain.model.Achievement
import com.davidbugayov.financeanalyzer.domain.usecase.AchievementEngine
import kotlinx.coroutines.flow.collectLatest

/**
 * Менеджер уведомлений о достижениях, который слушает AchievementEngine
 * и показывает уведомления о новых разблокированных достижениях
 */
@Composable
fun AchievementNotificationManager(
    achievementEngine: AchievementEngine?,
    modifier: Modifier = Modifier,
    onAchievementUnlocked: ((Achievement) -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    var currentNotification by remember { mutableStateOf<Achievement?>(null) }
    var showNotification by remember { mutableStateOf(false) }

    // Подписываемся на новые достижения
    LaunchedEffect(achievementEngine) {
        achievementEngine?.newAchievements?.collectLatest { achievement ->
            // Вызываем callback для внешней логики (например, аналитики)
            onAchievementUnlocked?.invoke(achievement)

            currentNotification = achievement
            showNotification = true
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Основной контент экрана
        content()

        // Уведомление о достижении поверх всего контента
        currentNotification?.let { achievement ->
            AchievementNotification(
                title = achievement.title,
                description = achievement.description,
                rewardCoins = achievement.rewardCoins,
                isVisible = showNotification,
                onDismiss = {
                    showNotification = false
                    // После скрытия анимации сбрасываем уведомление
                    currentNotification = null
                },
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
            )
        }
    }
}

/**
 * Глобальный объект для доступа к AchievementEngine из любого места
 */
object AchievementEngineProvider {
    private var _engine: AchievementEngine? = null

    fun initialize(engine: AchievementEngine) {
        _engine = engine
    }

    fun get(): AchievementEngine? = _engine
}
