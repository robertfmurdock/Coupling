package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.blue
import com.zegreatrob.coupling.client.dom.large
import com.zegreatrob.coupling.client.dom.lightGreen
import com.zegreatrob.coupling.client.dom.white
import com.zegreatrob.coupling.client.dom.yellow
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.party.PartyCard
import com.zegreatrob.coupling.client.party.PartySelectButton
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.add
import csstype.AlignItems
import csstype.AlignSelf
import csstype.BoxShadow
import csstype.ClassName
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
import react.FC
import react.PropsWithChildren
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.i
import react.dom.html.ReactHTML.span
import react.router.dom.Link

external interface ConfigHeaderProps : PropsWithChildren {
    var party: Party
}

private val styles = useStyles("ConfigHeader")

val ConfigHeader = FC<ConfigHeaderProps> { props ->
    val party = props.party
    div {
        css(styles.className) {
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
                viewHistoryButton(party, styles["viewHistoryButton"])
                pinListButton(party, styles["pinListButton"])
                statisticsButton(party, styles["statisticsButton"])
                viewRetireesButton(party, styles["retiredPlayersButton"])
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

fun ChildrenBuilder.viewHistoryButton(party: Party, className: ClassName = ClassName("")) = Link {
    to = "/${party.id.value}/history/"
    tabIndex = -1
    draggable = false
    add(CouplingButton(large, lightGreen, className)) {
        i { this.className = ClassName("fa fa-history") }
        +" History!"
    }
}

fun ChildrenBuilder.pinListButton(party: Party, className: ClassName = ClassName("")) = Link {
    to = "/${party.id.value}/pins/"
    tabIndex = -1
    draggable = false
    add(CouplingButton(large, white, className)) {
        i { this.className = ClassName("fa fa-peace") }
        +" Pin Bag!"
    }
}

fun ChildrenBuilder.statisticsButton(party: Party, className: ClassName = ClassName("")) = Link {
    to = "/${party.id.value}/statistics"
    tabIndex = -1
    draggable = false
    add(CouplingButton(large, blue, className = className)) {
        i { this.className = ClassName("fa fa-database") }
        +" Statistics!"
    }
}

fun ChildrenBuilder.viewRetireesButton(party: Party, className: ClassName = ClassName("")) = Link {
    to = "/${party.id.value}/players/retired"
    tabIndex = -1
    draggable = false
    add(CouplingButton(large, yellow, className)) {
        i { this.className = ClassName("fa fa-user-slash") }
        +" Retirees!"
    }
}
