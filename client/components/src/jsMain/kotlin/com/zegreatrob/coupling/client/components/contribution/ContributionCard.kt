package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.client.components.TiltedPlayerList
import com.zegreatrob.coupling.client.components.pngPath
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.emails
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import react.ChildrenBuilder
import react.Fragment
import react.Props
import react.create
import react.dom.html.ReactHTML.div
import web.cssom.AlignItems
import web.cssom.BackgroundRepeat
import web.cssom.BoxShadow
import web.cssom.Color
import web.cssom.Display
import web.cssom.FlexDirection
import web.cssom.FontWeight
import web.cssom.JustifySelf
import web.cssom.LineStyle
import web.cssom.Margin
import web.cssom.NamedColor
import web.cssom.None
import web.cssom.Overflow
import web.cssom.Position
import web.cssom.TextAlign
import web.cssom.TextWrap
import web.cssom.VerticalAlign
import web.cssom.deg
import web.cssom.em
import web.cssom.fr
import web.cssom.integer
import web.cssom.pct
import web.cssom.px
import web.cssom.repeat
import web.cssom.rgb
import web.cssom.rotatex
import web.cssom.scale
import web.cssom.translate
import web.cssom.url

external interface ContributionCardProps : Props {
    var contribution: Contribution
    var contributors: List<Player>
}

private val dateTimeFormat = LocalDateTime.Format {
    hour()
    chars(":")
    minute()
    chars(":")
    second()
    chars(", ")
    year()
    chars("-")
    monthNumber()
    chars("-")
    dayOfMonth()
}

@ReactFunc
val ContributionCard by nfc<ContributionCardProps> { (contribution, contributors) ->
    val shortId = contribution.id.asShortId()

    div {
        css {
            display = Display.flex
            alignItems = AlignItems.center
        }
        div {
            css {
                display = Display.inlineBlock
                borderStyle = LineStyle.outset
                borderColor = Color("rgb(252 224 140 / 50%)")
                backgroundColor = Color("rgb(252 224 140)")
                textAlign = TextAlign.center
                textDecoration = None.none
                boxShadow = BoxShadow(0.px, 0.1.em, 0.3.em, rgb(0, 0, 0, 0.6))
                color = NamedColor.black
                margin = Margin(0.px, 0.2.em, 0.4.em, 0.2.em)
                borderWidth = (0.2).em
                borderRadius = 1.em
                padding = 1.em
            }

            div {
                css {
                    margin = Margin(0.2.em, 0.px)
                    height = 2.5.em
                    verticalAlign = VerticalAlign.top
                    overflow = Overflow.visible
                    display = Display.flex
                    alignItems = AlignItems.center
                    flexDirection = FlexDirection.column
                    position = Position.relative
                    transform = scale(1.04)
                    perspective = 30.em
                    top = (-0.6).em
                }
                div {
                    css {
                        position = Position.absolute
                        overflow = Overflow.hidden
                        borderRadius = 1.em
                        top = 0.px
                        left = 0.px
                        right = 0.px
                        bottom = 0.px
                        transform = rotatex(20.deg)
                        backgroundColor = rgb(255, 255, 255, 0.4)
                        backgroundImage = url(pngPath("overlay"))
                        backgroundRepeat = BackgroundRepeat.repeatX
                        borderStyle = LineStyle.hidden
                        borderColor = Color("#00000054")
                        borderWidth = 1.px
                        fontWeight = FontWeight.bold
                    }
                }
                div {
                    css {
                        height = 1.3.em
                        zIndex = integer(100)
                        position = Position.absolute
                        top = 50.pct
                        left = 50.pct
                        transform = translate((-50).pct, (-50).pct)
                        fontSize = 1.4.em
                    }
                    div {
                        css {
                            display = Display.flex
                            alignItems = AlignItems.center
                            height = 1.4.em
                            textWrap = TextWrap.nowrap
                        }
                        +"${contribution.label} $shortId ${contribution.dateTime?.format()}"
                    }
                }
            }
            div {
                css {
                    display = Display.grid
                    gridTemplateColumns = repeat(4, 1.fr)
                    rowGap = 0.2.em
                    columnGap = 1.em
                }
                showProperty("ID") { +shortId }
                showProperty("Participants") {
                    div {
                        css {
                            display = Display.inlineFlex
                            maxWidth = 14.em
                            flexDirection = FlexDirection.column
                            alignItems = AlignItems.end
                        }
                        contribution.participantEmails.forEach { email ->
                            div { +email }
                        }
                    }
                }
                showOptionalProperty("Contribution Time", contribution.dateTime?.format())
                showOptionalProperty("Label", contribution.label)
                showOptionalProperty("Link", contribution.link)
                showOptionalProperty("Ease", contribution.ease)
                showOptionalProperty("Semver", contribution.semver)
                showOptionalProperty("Hash", contribution.hash?.asShortId())
                showOptionalProperty("First Commit", contribution.firstCommit?.asShortId())
                showOptionalProperty("Story", contribution.story)
                showOptionalProperty("Save Timestamp", contribution.createdAt.format())
            }
        }
        TiltedPlayerList(
            playerList = contribution.participantEmails.mapNotNull { email ->
                contributors.find { it.emails.contains(email) }
            }.toSet(),
            size = 50,
        )
    }
}

private fun String.asShortId() = substring(0, 7)

private fun <T> ChildrenBuilder.showOptionalProperty(attributeName: String, value: T?) {
    value?.let {
        showProperty<T>(attributeName, it)
    }
}

private fun <T> ChildrenBuilder.showProperty(attributeName: String, value: T & Any) {
    showProperty(attributeName) { +"$value" }
}

private fun ChildrenBuilder.showProperty(attributeName: String, value: ChildrenBuilder.() -> Unit) {
    div {
        css { justifySelf = JustifySelf.left }
        +("$attributeName:")
    }
    div {
        css {
            justifySelf = JustifySelf.right
            display = Display.inlineFlex
            maxWidth = 14.em
            flexDirection = FlexDirection.column
            alignItems = AlignItems.end
        }
        +Fragment.create(value)
    }
}

private fun Instant.format() = dateTimeFormat.format(toLocalDateTime(TimeZone.currentSystemDefault()))
