package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.tribe.TribeCard
import com.zegreatrob.coupling.client.tribe.TribeSelectButton
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.child
import kotlinx.css.*
import react.FC
import react.PropsWithChildren
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1

external interface ConfigHeaderProps : PropsWithChildren {
    var tribe: Tribe
}

private val styles = useStyles("ConfigHeader")

val ConfigHeader = FC<ConfigHeaderProps> { props ->
    div {
        className = styles.className
        div { child(TribeCard(props.tribe, 50)) }
        h1 {
            cssDiv(css = {
                display = Display.flex
                alignItems = Align.baseline
                descendants { verticalAlign = VerticalAlign.middle }
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
    }
}
