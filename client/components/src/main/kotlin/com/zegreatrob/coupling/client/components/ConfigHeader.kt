package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.party.PartyCard
import com.zegreatrob.coupling.client.components.player.addPlayerButton
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.nfc
import csstype.AlignItems
import csstype.AlignSelf
import csstype.BoxShadow
import csstype.Color
import csstype.Display
import csstype.FlexDirection
import csstype.Margin
import csstype.Padding
import csstype.TextAlign
import csstype.TextDecoration
import csstype.VerticalAlign
import csstype.WhiteSpace
import csstype.number
import csstype.pt
import csstype.px
import csstype.rgba
import emotion.react.css
import react.ChildrenBuilder
import react.PropsWithChildren
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.span

external interface ConfigHeaderProps : PropsWithChildren {
    var party: Party
}

val ConfigHeader by nfc<ConfigHeaderProps> { props ->
    val party = props.party
    div {
        css {
            textAlign = TextAlign.left
            margin = 5.px
        }
        div {
            css {
                display = Display.flex
                flexDirection = FlexDirection.row
                alignItems = AlignItems.center
                whiteSpace = WhiteSpace.nowrap
            }
            add(PartyCard(party, 75))
            partyControls(props, party)
        }
    }
}

private fun ChildrenBuilder.partyControls(props: ConfigHeaderProps, party: Party) = div {
    css {
        flexGrow = number(2.0)
    }
    h1 {
        css {
            display = Display.flex
            flexDirection = FlexDirection.column
            alignItems = AlignItems.center
            marginLeft = 15.px
            textDecoration = TextDecoration.underline
            flexGrow = number(2.0)
        }
        topControlRow(props)
        div {
            css {
                margin = Margin(0.px, 20.px)
                display = Display.flex
            }
            div {
                css {
                    display = Display.inlineFlex
                    alignItems = AlignItems.center
                    borderRadius = 20.px
                    padding = Padding(5.px, 5.px)
                    margin = Margin(2.px, 2.px)
                    fontSize = 0.pt
                    backgroundColor = Color("#00000014")
                    boxShadow = BoxShadow(1.px, 1.px, 3.px, rgba(0, 0, 0, 0.6))
                }
                settingsButton(party)
                addPlayerButton { this.partyId = party.id }
                viewHistoryButton(party)
                pinListButton(party)
                statisticsButton(party)
                viewRetireesButton(party)
            }
        }
    }
}

private fun ChildrenBuilder.topControlRow(props: ConfigHeaderProps) = div {
    css {
        display = Display.inlineBlock
        marginLeft = 15.px
        textDecoration = TextDecoration.underline
        flexGrow = number(2.0)
        alignSelf = AlignSelf.stretch
        "*" { verticalAlign = VerticalAlign.middle }
    }
    div {
        css {
            display = Display.flex
            alignItems = AlignItems.baseline
        }
        span {
            css { flexGrow = number(2.0) }
            +props.children
        }
        span {
            css { margin = Margin(0.px, 20.px) }
            PartySelectButton()
            LogoutButton()
            GqlButton()
            NotificationButton()
        }
    }
}
