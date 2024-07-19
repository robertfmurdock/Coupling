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
import react.Props
import react.ReactNode
import react.create
import react.dom.html.ReactHTML.div
import web.cssom.AlignItems
import web.cssom.Display
import web.cssom.FlexDirection
import web.cssom.JustifySelf
import web.cssom.em
import web.cssom.fr
import web.cssom.repeat

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
            display = Display.grid
            gridTemplateColumns = repeat(4, 1.fr)
            rowGap = 0.2.em
            columnGap = 1.em
        }
        showProperty("ID", contribution.id)
        showProperty(
            "Participants",
            div.create {
                css {
                    display = Display.inlineFlex
                    maxWidth = 14.em
                    flexDirection = FlexDirection.column
                    alignItems = AlignItems.end
                }
                contribution.participantEmails.forEach { email ->
                    div { +email }
                }
            },
        )
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

private fun <T> ChildrenBuilder.showOptionalProperty(attributeName: String, value: T?) {
    value?.let {
        showProperty<T>(attributeName, it)
    }
}

private fun <T> ChildrenBuilder.showProperty(attributeName: String, value: T & Any) {
    showProperty(attributeName, ReactNode("$value"))
}

private fun ChildrenBuilder.showProperty(attributeName: String, value: ReactNode) {
    div {
        css { justifySelf = JustifySelf.left }
        +("$attributeName:")
    }
    div {
        css { justifySelf = JustifySelf.right }
        +value
    }
}

private fun Instant.format() = dateTimeFormat.format(toLocalDateTime(TimeZone.currentSystemDefault()))
