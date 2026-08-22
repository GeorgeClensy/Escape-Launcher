package com.geecee.escapelauncher.core.ui.utils

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Performs haptic feedback
 *
 * @param hapticFeedback HapticFeedback instance
 * @param enabled Whether haptic feedback is enabled from settings
 */
fun doHapticFeedBack(hapticFeedback: HapticFeedback, enabled: Boolean) {
    if (enabled) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }
}