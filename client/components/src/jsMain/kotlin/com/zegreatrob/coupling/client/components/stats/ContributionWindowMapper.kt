package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.client.components.graphing.ContributionWindow
import kotlin.time.Duration.Companion.days

fun ContributionWindow.toValue() = when (this) {
    ContributionWindow.All -> null
    ContributionWindow.Year -> 365.days
    ContributionWindow.HalfYear -> (365 / 2.0).days
    ContributionWindow.Quarter -> (365 / 4.0).days
    ContributionWindow.Month -> 30.days
    ContributionWindow.Week -> 7.days
}
