package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.pairassignments.pinListButton
import com.zegreatrob.coupling.client.pairassignments.statisticsButton
import com.zegreatrob.coupling.client.pairassignments.viewHistoryButton
import com.zegreatrob.coupling.client.pairassignments.viewRetireesButton
import com.zegreatrob.coupling.client.tribe.TribeCard
import com.zegreatrob.coupling.client.tribe.TribeSelectButton
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.child
import kotlinx.css.*
import kotlinx.css.properties.TextDecorationLine
import kotlinx.css.properties.boxShadow
import kotlinx.css.properties.textDecoration
import react.ChildrenBuilder
import react.FC
import react.PropsWithChildren
import react.dom.html.ReactHTML.div

external interface ConfigHeaderProps : PropsWithChildren {
    var tribe: Tribe
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
        }) {
            child(TribeCard(tribe, 75))
            tribeControls(props, tribe)
        }
    }
}

private fun ChildrenBuilder.tribeControls(props: ConfigHeaderProps, tribe: Tribe) = cssDiv(css = {
    flexGrow = 2.0
}) {
    cssH1(css = {
        display = Display.flex
        flexDirection = FlexDirection.column
        alignItems = Align.end
    }) {
        topControlRow(props)
        cssDiv(css = { margin(0.px, 20.px) }) {
            cssDiv(css = {
                display = Display.inlineBlock
                borderRadius = 20.px
                padding(5.px)
                backgroundColor = Color("#d5cdc3")
                boxShadow(rgba(0, 0, 0, 0.6), 1.px, 1.px, 3.px)
            }) {
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
            props.children()
        }
        cssSpan(css = { margin(0.px, 20.px) }) {
            TribeSelectButton()
            LogoutButton()
            GqlButton()
            NotificationButton()
        }
    }
}
