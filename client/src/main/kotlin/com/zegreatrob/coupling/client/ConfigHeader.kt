package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.dom.*
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.tribe.TribeCard
import com.zegreatrob.coupling.client.tribe.TribeSelectButton
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.minreact.child
import csstype.ClassName
import kotlinx.css.*
import kotlinx.css.properties.TextDecorationLine
import kotlinx.css.properties.boxShadow
import kotlinx.css.properties.textDecoration
import react.ChildrenBuilder
import react.FC
import react.PropsWithChildren
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.i
import react.router.dom.Link

external interface ConfigHeaderProps : PropsWithChildren {
    var tribe: Party
}

private val styles = useStyles("ConfigHeader")

val ConfigHeader = FC<ConfigHeaderProps> { props ->
    val tribe = props.tribe
    div {
        className = styles.className
        cssDiv(css = {
            display = Display.flex
            flexDirection = FlexDirection.row
            alignItems = Align.center
            whiteSpace = WhiteSpace.nowrap
        }) {
            child(TribeCard(tribe, 75))
            tribeControls(props, tribe)
        }
    }
}

private fun ChildrenBuilder.tribeControls(props: ConfigHeaderProps, tribe: Party) = cssDiv(css = {
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
                settingsButton(tribe)
                viewHistoryButton(tribe, styles["viewHistoryButton"])
                pinListButton(tribe, styles["pinListButton"])
                statisticsButton(tribe, styles["statisticsButton"])
                viewRetireesButton(tribe, styles["retiredPlayersButton"])
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
            TribeSelectButton()
            LogoutButton()
            GqlButton()
            NotificationButton()
        }
    }
}

fun ChildrenBuilder.viewHistoryButton(tribe: Party, className: ClassName = ClassName("")) = Link {
    to = "/${tribe.id.value}/history/"
    tabIndex = -1
    draggable = false
    child(CouplingButton(large, lightGreen, className)) {
        i { this.className = ClassName("fa fa-history") }
        +" History!"
    }
}

fun ChildrenBuilder.pinListButton(tribe: Party, className: ClassName = ClassName("")) = Link {
    to = "/${tribe.id.value}/pins/"
    tabIndex = -1
    draggable = false
    child(CouplingButton(large, white, className)) {
        i { this.className = ClassName("fa fa-peace") }
        +" Pin Bag!"
    }
}

fun ChildrenBuilder.statisticsButton(tribe: Party, className: ClassName = ClassName("")) = Link {
    to = "/${tribe.id.value}/statistics"
    tabIndex = -1
    draggable = false
    child(CouplingButton(large, blue, className = className)) {
        i { this.className = ClassName("fa fa-database") }
        +" Statistics!"
    }
}

fun ChildrenBuilder.viewRetireesButton(tribe: Party, className: ClassName = ClassName("")) = Link {
    to = "/${tribe.id.value}/players/retired"
    tabIndex = -1
    draggable = false
    child(CouplingButton(large, yellow, className)) {
        i { this.className = ClassName("fa fa-user-slash") }
        +" Retirees!"
    }
}

fun ChildrenBuilder.settingsButton(tribe: Party, className: ClassName = ClassName("")) = Link {
    to = "/${tribe.id.value}/edit"
    tabIndex = -1
    draggable = false
    child(CouplingButton(large, black, className) {
        fontSize = 24.px
        padding(1.px, 4.px, 2.px)
    }) {
        i { this.className = ClassName("fa fa-cog") }
    }
}
