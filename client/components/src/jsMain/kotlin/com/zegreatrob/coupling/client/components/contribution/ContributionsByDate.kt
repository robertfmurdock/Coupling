package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.model.Contribution
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

val contributionsByDate: (Contribution) -> LocalDate? = { contribution ->
    contribution.dateTime
        ?.toLocalDateTime(TimeZone.Companion.currentSystemDefault())
        ?.date
}
