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
import react.create
import react.dom.aria.ariaLabel
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import react.useMemo
import react.useState
import web.cssom.Color
import web.cssom.Display
import web.cssom.WhiteSpace
import web.cssom.px
import web.html.InputType
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
    val (shouldFake, setShouldFake) = useState(false)
    val fakeContributions: List<Pair<CouplingPair, List<Contribution>>> =
        useMemo { allPairs.map { it to generateFakeContributions() } }
    val allPairContributions: List<Pair<CouplingPair, List<Contribution>>> =
        if (shouldFake) {
            fakeContributions
        } else {
            props.pairs.mapNotNull {
                it.players?.elements?.toCouplingPair()
                    ?.let { pair -> pair to (it.contributions?.elements ?: emptyList()) }
            }
        }
    val allLabels = allPairContributions.flatMap { it.second.map(Contribution::label) }.toSet()
    val (selectedPairs, setSelectedPairs) = useState(emptyList<CouplingPair>())
    val (selectedLabelFilter, setSelectedLabelFilter) = useState<String?>(null)
    println("selected label filter '$selectedLabelFilter'")
    val filteredData = allPairContributions.applyFilters(
        selectedPairs,
        selectedLabelFilter,
    )
    div {
        PageFrame(borderColor = Color("#e8e8e8"), backgroundColor = Color("#dcd9d9")) {
            ConfigHeader {
                this.party = partyDetails
                +"Statistics"
            }
            div {
                css {
                    whiteSpace = WhiteSpace.nowrap
                }
                div {
                    select {
                        onChange = { event ->
                            setSelectedLabelFilter(event.target.value.let { if (it == "NULL") null else it })
                        }
                        option {
                            value = "NULL"
                            +"No label filter"
                        }
                        allLabels.map { label ->
                            option {
                                value = label
                                +label
                            }
                        }
                    }
                }
                div {
                    css {
                        display = Display.inlineFlex
                        marginLeft = 20.px
                    }
                    div {
                        css { display = Display.inlineBlock }
                        label {
                            ariaLabel = "Fake the data"
                            +"Fake the data"
                            input {
                                type = InputType.checkbox
                                value = shouldFake
                                onChange = { setShouldFake(!shouldFake) }
                            }
                        }
                        PairSelector(
                            pairs = allPairContributions.toMap()
                                .filterValues(List<Contribution>::isNotEmpty).keys.toList(),
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
                        if (selectedPairs.isNotEmpty() && filteredData.flatMap { it.second }.isNotEmpty()) {
                            MyResponsiveLine {
                                legend = "Pair Commits Over Time"
                                data = pairingLineData(filteredData)
                                tooltip = { point ->
                                    div.create {
                                        css {
                                            backgroundColor = Color("rgb(0 0 0 / 14%)")
                                            padding = 10.px
                                            borderRadius = 20.px
                                        }
                                        div { +"${point.xFormatted} - ${point.yFormatted}" }
                                        div { +"${point.context}" }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun List<Pair<CouplingPair, List<Contribution>>>.applyFilters(
    selectedPairs: List<CouplingPair>,
    selectedLabelFilter: String?,
): List<Pair<CouplingPair, List<Contribution>>> {
    val transforms = if (selectedLabelFilter != null) {
        listOf(selectedLabelTransform(selectedLabelFilter))
    } else {
        emptyList()
    }

    return transforms.fold(filter { selectedPairs.contains(it.first) }) { list, transform ->
        list.map(transform)
    }
}

private fun selectedLabelTransform(selectedLabelFilter: String) =
    { (pair, contributions): Pair<CouplingPair, List<Contribution>> ->
        pair to contributions.filter { it.label == selectedLabelFilter }
    }

val random = Random(10)

private fun pairingLineData(selectedPairs: List<Pair<CouplingPair, List<Contribution>>>): Array<NivoLineData> =
    selectedPairs.map { pairContributionLine(it.first, it.second) }.toTypedArray()

private fun pairContributionLine(couplingPair: CouplingPair, contributions: List<Contribution>) =
    NivoLineData(
        couplingPair.joinToString("-") { it.name },
        contributions.groupBy { contribution ->
            contribution.dateTime
                ?.toLocalDateTime(TimeZone.currentSystemDefault())
                ?.date
        }.mapNotNull {
            val date = it.key ?: return@mapNotNull null
            NinoLinePoint(
                x = date.atTime(0, 0).toInstant(TimeZone.currentSystemDefault()).toJSDate(),
                y = it.value.size,
                context = it.value.mapNotNull(Contribution::label).toSet().joinToString(", "),
            )
        }.toTypedArray(),
    )

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
    if (Random.nextBoolean()) "fake" else "alternate",
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
                dayDate.toLocalDateTime(TimeZone.currentSystemDefault()).date.atTime(timeOfDay, 0, 0)
            }
        } else {
            emptyList()
        }
    }
    return commitTimes
}

private fun Int.isWeekday() = (1..5).contains(dayOfWeek())

private fun Int.dayOfWeek() = this % 7
