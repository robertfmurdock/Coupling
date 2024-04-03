package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.components.ConfigHeader
import com.zegreatrob.coupling.client.components.PageFrame
import com.zegreatrob.coupling.client.components.stats.NinoLinePoint
import com.zegreatrob.coupling.client.components.stats.NivoLineData
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJSDate
import kotlinx.datetime.toLocalDateTime
import react.Props
import react.dom.html.ReactHTML.div
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
    val (party) = props
    div {
        PageFrame(borderColor = Color("#e8e8e8"), backgroundColor = Color("#dcd9d9")) {
            ConfigHeader {
                this.party = party
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
                        css {
                            width = 600.px
                            height = 600.px
                            backgroundColor = Color("white")
                        }
                        MyResponsiveLine {
                            legend = "Pair Commits Over Time"
                            data = stubPairingLineData()
                        }
                    }
                }
            }
        }
    }
}

val random = Random(10)

private fun stubPairingLineData(): Array<NivoLineData> {
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

    console.log(commitTimes)

    return arrayOf(
        NivoLineData(
            "1",
            commitTimes
                .groupBy { it.date }
                .map {
                    NinoLinePoint(
                        x = it.key.atTime(0, 0).toInstant(TimeZone.currentSystemDefault()).toJSDate(),
                        y = it.value.size,
                    )
                }
                .toTypedArray(),
        ),
    )
}

private fun Int.isWeekday() = (1..5).contains(dayOfWeek())

private fun Int.dayOfWeek() = this % 7
