package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.graphing.ContributionWindow

fun ContributionWindow.toGql() = when (this) {
    ContributionWindow.All -> com.zegreatrob.coupling.sdk.schema.type.ContributionWindow.All
    ContributionWindow.Year -> com.zegreatrob.coupling.sdk.schema.type.ContributionWindow.Year
    ContributionWindow.HalfYear -> com.zegreatrob.coupling.sdk.schema.type.ContributionWindow.HalfYear
    ContributionWindow.Quarter -> com.zegreatrob.coupling.sdk.schema.type.ContributionWindow.Quarter
    ContributionWindow.Month -> com.zegreatrob.coupling.sdk.schema.type.ContributionWindow.Month
    ContributionWindow.Week -> com.zegreatrob.coupling.sdk.schema.type.ContributionWindow.Week
}
