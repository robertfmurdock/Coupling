package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.black
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
import com.zegreatrob.minreact.child
import csstype.ClassName
import kotlinx.css.Align
import kotlinx.css.Color
import kotlinx.css.Display
import kotlinx.css.FlexDirection
import kotlinx.css.VerticalAlign
import kotlinx.css.WhiteSpace
import kotlinx.css.alignItems
import kotlinx.css.alignSelf
import kotlinx.css.backgroundColor
import kotlinx.css.borderRadius
import kotlinx.css.display
import kotlinx.css.flexDirection
import kotlinx.css.flexGrow
import kotlinx.css.fontSize
import kotlinx.css.margin
import kotlinx.css.marginLeft
import kotlinx.css.padding
import kotlinx.css.properties.TextDecorationLine
import kotlinx.css.properties.boxShadow
import kotlinx.css.properties.textDecoration
import kotlinx.css.pt
import kotlinx.css.px
import kotlinx.css.rgba
import kotlinx.css.verticalAlign
import kotlinx.css.whiteSpace
import react.ChildrenBuilder
import react.FC
import react.PropsWithChildren
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.i
import react.router.dom.Link

external interface ConfigHeaderProps : PropsWithChildren {
    var party: Party
}

private val styles = useStyles("ConfigHeader")

val ConfigHeader = FC<ConfigHeaderProps> { props ->
    val party = props.party
    div {
        className = styles.className
        cssDiv(css = {
            display = Display.flex
            flexDirection = FlexDirection.row
            alignItems = Align.center
            whiteSpace = WhiteSpace.nowrap
        }) {
            child(PartyCard(party, 75))
            partyControls(props, party)
        }
    }
}

private fun ChildrenBuilder.partyControls(props: ConfigHeaderProps, party: Party) = cssDiv(css = {
    flexGrow = 2.0
}) {
    cssH1(css = {
        display = Display.flex
        flexDirection = FlexDirection.column
        alignItems = Align.center
    }) {
        topControlRow(props)
        cssDiv(css = {
            margin(0.px, 20.px)
            display = Display.flex
        }) {
            cssDiv(css = {
                display = Display.inlineFlex
                alignItems = Align.center
                borderRadius = 20.px
                padding(5.px)
                margin(2.px)
                fontSize = 0.pt
                backgroundColor = Color("#00000014")
                boxShadow(rgba(0, 0, 0, 0.6), 1.px, 1.px, 3.px)
            }) {
                settingsButton(party)
                viewHistoryButton(party, styles["viewHistoryButton"])
                pinListButton(party, styles["pinListButton"])
                statisticsButton(party, styles["statisticsButton"])
                viewRetireesButton(party, styles["retiredPlayersButton"])
            }
        }
    }
}

private fun ChildrenBuilder.topControlRow(props: ConfigHeaderProps) = cssDiv(css = {
    display = Display.inlineBlock
    marginLeft = 15.px
    textDecoration(TextDecorationLine.underline)
    flexGrow = 2.0
    alignSelf = Align.stretch
    descendants { verticalAlign = VerticalAlign.middle }
}) {
    cssDiv(css = {
        display = Display.flex
        alignItems = Align.baseline
    }) {
        cssSpan(css = { flexGrow = 2.0 }) {
            +props.children
        }
        cssSpan(css = { margin(0.px, 20.px) }) {
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
    child(CouplingButton(large, lightGreen, className)) {
        i { this.className = ClassName("fa fa-history") }
        +" History!"
    }
}

fun ChildrenBuilder.pinListButton(party: Party, className: ClassName = ClassName("")) = Link {
    to = "/${party.id.value}/pins/"
    tabIndex = -1
    draggable = false
    child(CouplingButton(large, white, className)) {
        i { this.className = ClassName("fa fa-peace") }
        +" Pin Bag!"
    }
}

fun ChildrenBuilder.statisticsButton(party: Party, className: ClassName = ClassName("")) = Link {
    to = "/${party.id.value}/statistics"
    tabIndex = -1
    draggable = false
    child(CouplingButton(large, blue, className = className)) {
        i { this.className = ClassName("fa fa-database") }
        +" Statistics!"
    }
}

fun ChildrenBuilder.viewRetireesButton(party: Party, className: ClassName = ClassName("")) = Link {
    to = "/${party.id.value}/players/retired"
    tabIndex = -1
    draggable = false
    child(CouplingButton(large, yellow, className)) {
        i { this.className = ClassName("fa fa-user-slash") }
        +" Retirees!"
    }
}

fun ChildrenBuilder.settingsButton(party: Party, className: ClassName = ClassName("")) = Link {
    to = "/${party.id.value}/edit"
    tabIndex = -1
    draggable = false
    child(
        CouplingButton(large, black, className) {
            fontSize = 24.px
            padding(1.px, 4.px, 2.px)
        }
    ) {
        i { this.className = ClassName("fa fa-cog") }
    }
}
