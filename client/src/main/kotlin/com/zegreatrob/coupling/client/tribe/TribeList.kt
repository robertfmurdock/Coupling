package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.dom.couplingButton
import com.zegreatrob.coupling.client.dom.green
import com.zegreatrob.coupling.client.dom.orange
import com.zegreatrob.coupling.client.dom.supersize
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.reactFunction
import react.Props
import react.RBuilder
import react.dom.div
import react.router.dom.Link

data class TribeListProps(val tribes: List<Tribe>) : Props

private val styles = useStyles("tribe/TribeList")

val TribeList = reactFunction<TribeListProps> { (tribes) ->
    div(classes = styles.className) {
        div { aboutButton() }
        div {
            tribes.forEach { tribe ->
                tribeCard(TribeCardProps(tribe), key = tribe.id.value)
            }
        }
        div { newTribeButton(styles["newTribeButton"]) }
    }
}

private fun RBuilder.aboutButton() = Link {
    attrs.to = "/about"
    couplingButton(supersize, orange) { +"About Coupling" }
}

private fun RBuilder.newTribeButton(className: String) = Link {
    attrs.to = "/new-tribe/"
    couplingButton(supersize, green, className) {
        +"Add a new tribe!"
    }
}
