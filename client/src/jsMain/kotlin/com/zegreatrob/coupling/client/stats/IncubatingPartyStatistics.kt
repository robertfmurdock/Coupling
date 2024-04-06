package com.zegreatrob.coupling.client.stats

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.client.components.ConfigHeader
import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.coupling.client.components.stats.NinoLinePoint
import com.zegreatrob.coupling.client.components.stats.NivoLineData
import com.zegreatrob.coupling.client.components.stats.PairSelector
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.toCouplingPair
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJSDate
import kotlinx.datetime.toLocalDateTime
import react.Props
import react.dom.html.ReactHTML.div
import react.useMemo
import react.useState
import web.cssom.Color
import web.cssom.Display
import web.cssom.WhiteSpace
import web.cssom.px
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

external interface IncubatingPartyStatisticsProps : Props {
    var party: PartyDetails
    var players: List<Player>
    var pairs: List<PlayerPair>
}

@ReactFunc
val IncubatingPartyStatistics by nfc<IncubatingPartyStatisticsProps> { props ->
    val (partyDetails) = props
    val allPairs = props.pairs.mapNotNull { it.players?.elements?.toCouplingPair() }
    val allPairContributions = useMemo { allPairs.map { it to generateFakeContributions() } }
    val (selectedPairs, setSelectedPairs) = useState(emptyList<CouplingPair>())
    div {
        PageFrame(borderColor = Color("#e8e8e8"), backgroundColor = Color("#dcd9d9")) {
            ConfigHeader {
                this.party = partyDetails
                +"Statistics"
            }
            div {
                css {
                    whiteSpace = WhiteSpace.nowrap
                    display = Display.inlineFlex
                }
                div {
                    css {
                        display = Display.inlineBlock
                        marginLeft = 20.px
                    }
                    div {
                        css { display = Display.inlineBlock }
                        PairSelector(
                            pairs = allPairs,
                            onSelectionChange = setSelectedPairs::invoke,
                        )
                    }
                    div {
                        css {
                            display = Display.inlineBlock
                            width = 600.px
                            height = 600.px
                            backgroundColor = Color("white")
                        }
                        MyResponsiveLine {
                            legend = "Pair Commits Over Time"
                            data = pairingLineData(allPairContributions.filter { selectedPairs.contains(it.first) })
                        }
                    }
                }
            }
        }
    }
}

val random = Random(10)

private fun pairingLineData(selectedPairs: List<Pair<CouplingPair, List<Contribution>>>): Array<NivoLineData> {
    return selectedPairs.map { pairContributionLine(it.first, it.second) }.toTypedArray()
}

private fun pairContributionLine(couplingPair: CouplingPair, contributions: List<Contribution>): NivoLineData {
    val commitDateTimes = contributions
        .mapNotNull { it.dateTime }
    return NivoLineData(
        couplingPair.joinToString("-") { it.name },
        commitDateTimes
            .map { it.toLocalDateTime(TimeZone.currentSystemDefault()) }
            .groupBy(LocalDateTime::date)
            .map {
                NinoLinePoint(
                    x = it.key.atTime(0, 0).toInstant(TimeZone.currentSystemDefault()).toJSDate(),
                    y = it.value.size,
                )
            }
            .toTypedArray(),
    )
}

private fun generateFakeContributions() = generateCommitTimes().map(LocalDateTime::toFakeContribution)

private fun LocalDateTime.toFakeContribution() = Contribution(
    id = "${uuid4()}",
    createdAt = Clock.System.now(),
    dateTime = toInstant(TimeZone.currentSystemDefault()),
    null,
    null,
    null,
    null,
    null,
    emptySet(),
    null,
    null,
)

private fun generateCommitTimes(): List<LocalDateTime> {
    val today = Clock.System.now()
    val numberOfDays = 30
    val monthAgo = today.minus(duration = numberOfDays.days)

    val commitTimes = (1..numberOfDays).flatMap { day ->
        if (day.isWeekday()) {
            val dayDate = monthAgo.plus(day.days)
            val numberOfCommits = random.nextInt(0, 6)
            (0..numberOfCommits).map {
                val timeOfDay = random.nextInt(9, 17)
                dayDate.toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
                    .atTime(timeOfDay, 0, 0)
            }
        } else {
            emptyList()
        }
    }
    return commitTimes
}

private fun Int.isWeekday() = (1..5).contains(dayOfWeek())

private fun Int.dayOfWeek() = this % 7
