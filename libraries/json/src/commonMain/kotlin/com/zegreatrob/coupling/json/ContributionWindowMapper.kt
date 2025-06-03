package com.zegreatrob.coupling.json

import kotlin.time.Duration.Companion.days

fun GqlContributionWindow.toModel() = when (this) {
    GqlContributionWindow.All -> null
    GqlContributionWindow.Year -> 365.days
    GqlContributionWindow.HalfYear -> (365 / 2.0).days
    GqlContributionWindow.Quarter -> (365 / 4.0).days
    GqlContributionWindow.Month -> 30.days
    GqlContributionWindow.Week -> 7.days
}
