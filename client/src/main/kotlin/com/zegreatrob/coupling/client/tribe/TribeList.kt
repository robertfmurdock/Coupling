package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.model.tribe.Tribe
import kotlinx.html.classes
import react.RBuilder
import react.RProps
import react.dom.button
import react.dom.div
import react.router.dom.navLink

data class TribeListProps(val tribes: List<Tribe>, val pathSetter: (String) -> Unit) : RProps

object TribeList : FRComponent<TribeListProps>(provider()) {

    val styles = useStyles("tribe/TribeList")

    override fun render(props: TribeListProps) = reactElement {
        val (tribes, pathSetter) = props
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

    private fun RBuilder.aboutButton() = navLink(to = "/about") {
        button(classes = "super orange button") { +"About Coupling" }
    }

    private fun RBuilder.newTribeButton(className: String) = navLink(to = "/new-tribe/") {
        button(classes = "super green button") {
            attrs { classes += className }
            +"Add a new tribe!"
        }
    }

}
