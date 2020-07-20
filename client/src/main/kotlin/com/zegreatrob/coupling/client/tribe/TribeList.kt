package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.minreact.external.react.reactFunction
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.tribe.Tribe
import kotlinx.html.classes
import react.RBuilder
import react.RProps
import react.dom.button
import react.dom.div
import react.router.dom.routeLink

data class TribeListProps(val tribes: List<Tribe>, val pathSetter: (String) -> Unit) : RProps

private val styles = useStyles("tribe/TribeList")

val TribeList =
    reactFunction<TribeListProps> { (tribes, pathSetter) ->
        div(classes = styles.className) {
            div { aboutButton() }
            div {
                tribes.forEach { tribe ->
                    tribeCard(TribeCardProps(tribe, pathSetter = pathSetter), key = tribe.id.value)
                }
            }
            div { newTribeButton(styles["newTribeButton"]) }
        }
    }

private fun RBuilder.aboutButton() = routeLink(to = "/about") {
    button(classes = "super orange button") { +"About Coupling" }
}

private fun RBuilder.newTribeButton(className: String) = routeLink(to = "/new-tribe/") {
    button(classes = "super green button") {
        attrs { classes += className }
        +"Add a new tribe!"
    }
}
