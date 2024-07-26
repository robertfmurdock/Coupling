package com.zegreatrob.coupling.json

import kotlin.time.Duration.Companion.days

fun GqlContributionWindow.toModel() = when (this) {
    GqlContributionWindow.All -> null
    GqlContributionWindow.Year -> 365.days
    GqlContributionWindow.Quarter -> (365 / 4).days
    GqlContributionWindow.Month -> 30.days
    GqlContributionWindow.Week -> 7.days
}
