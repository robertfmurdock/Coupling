package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.client.components.format
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import kotools.types.text.NotBlankString
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
import web.html.HTMLElement

external interface ContributionCardProps : Props {
    @Suppress("INLINE_CLASS_IN_EXTERNAL_DECLARATION_WARNING")
    var partyId: PartyId
    var contribution: Contribution
    var players: List<Player>
    var onPlayerClick: ((Player, HTMLElement) -> Unit)?
}

@ReactFunc
val ContributionCard by nfc<ContributionCardProps> { props ->
    val contribution = props.contribution
    val shortId = contribution.id.value.asShortId()

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
            ContributionCardHeader(props.partyId, contribution, props.players, props.onPlayerClick)
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
                            div {
                                key = email
                                +email
                            }
                        }
                    }
                }
                showOptionalProperty("Name", contribution.name)
                showOptionalProperty("Label", contribution.label)
                showOptionalProperty("Link", contribution.link?.substring(0, 32)?.plus("..."))
                showOptionalProperty("Ease", contribution.ease)
                showOptionalProperty("Semver", contribution.semver)
                showOptionalProperty("Story", contribution.story)
                showOptionalProperty("Commit Count", contribution.commitCount)
                showOptionalProperty("Hash", contribution.hash?.asShortId())
                showOptionalProperty("First Commit", contribution.firstCommit?.asShortId())
                showOptionalProperty("First Commit Time", contribution.firstCommitDateTime?.format())
                showOptionalProperty("Integration Time", contribution.integrationDateTime?.format())
                showOptionalProperty("Contribution Time", contribution.dateTime?.format())
                showOptionalProperty("Save Timestamp", contribution.createdAt.format())
                showOptionalProperty("Cycle Time", contribution.cycleTime)
            }
        }
    }
}

fun NotBlankString.asShortId() = toString().substring(0, 7)
fun String.asShortId() = toString().substring(0, 7)

private fun <T> ChildrenBuilder.showOptionalProperty(attributeName: String, value: T?) {
    value?.let {
        showProperty<T>(attributeName, it)
    }
}

private fun <T> ChildrenBuilder.showProperty(attributeName: String, value: T & Any) {
    showProperty(attributeName) { +"$value" }
}

private fun ChildrenBuilder.showProperty(attributeName: String, value: ChildrenBuilder.() -> Unit) {
    Fragment {
        key = attributeName
        div {
            css { justifySelf = JustifySelf.left }
            +"$attributeName:"
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
}
