package com.zegreatrob.coupling.server.entity.contribution

import com.zegreatrob.coupling.action.stats.halfwayValue
import com.zegreatrob.coupling.json.JsonContributionStatistics
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyElement

val toSerializableContributionStatistics = { contributions: List<Record<PartyElement<Contribution>>> ->
    val cycleTimeContributions = contributions.elements.mapNotNull(Contribution::cycleTime)
    JsonContributionStatistics(
        count = contributions.count(),
        medianCycleTime = cycleTimeContributions.sorted().halfwayValue(),
        withCycleTimeCount = cycleTimeContributions.size,
    )
}
