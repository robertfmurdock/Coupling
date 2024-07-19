package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.model.Contribution
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
import web.cssom.BoxShadow
import web.cssom.Color
import web.cssom.Display
import web.cssom.FlexDirection
import web.cssom.JustifySelf
import web.cssom.LineStyle
import web.cssom.Margin
import web.cssom.NamedColor
import web.cssom.None
import web.cssom.TextAlign
import web.cssom.em
import web.cssom.fr
import web.cssom.px
import web.cssom.repeat
import web.cssom.rgb

external interface ContributionCardProps : Props {
    var contribution: Contribution
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
val ContributionCard by nfc<ContributionCardProps> { (contribution) ->
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
                display = Display.grid
                gridTemplateColumns = repeat(4, 1.fr)
                rowGap = 0.2.em
                columnGap = 1.em
            }
            showProperty("ID") { +contribution.id.substring(0, 7) }
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
            showOptionalProperty("Hash", contribution.hash)
            showOptionalProperty("First Commit", contribution.firstCommit)
            showOptionalProperty("Story", contribution.story)
            showOptionalProperty("Save Timestamp", contribution.createdAt.format())
        }
    }
}

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
