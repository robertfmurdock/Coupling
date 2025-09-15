package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.client.components.graphing.external.nivo.NivoPoint
import com.zegreatrob.coupling.model.Contribution
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.time.toJSDate

fun timeByContributionCountPoint(entry: Map.Entry<LocalDate?, List<Contribution>>): NivoPoint? {
    val date = entry.key ?: return null
    return NivoPoint(
        x = date.atTime(0, 0).toInstant(TimeZone.Companion.currentSystemDefault()).toJSDate(),
        y = entry.value.size,
        context = entry.value.mapNotNull(Contribution::label).toSet().joinToString(", "),
    )
}
