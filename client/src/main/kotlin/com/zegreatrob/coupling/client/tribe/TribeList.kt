package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.dom.*
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import react.RBuilder
import react.dom.div
import react.router.dom.Link

data class TribeList(val tribes: List<Tribe>) : DataProps<TribeList> {
    override val component: TMFC<TribeList> get() = tribeList
}

private val styles = useStyles("tribe/TribeList")

val tribeList = reactFunction<TribeList> { (tribes) ->
    div(classes = styles.className) {
        div { aboutButton() }
        div {
            tribes.forEach { tribe ->
                tribeCard(TribeCard(tribe), key = tribe.id.value)
            }
        }
        div { newTribeButton(styles["newTribeButton"]) }
    }
}

private fun RBuilder.aboutButton() = Link {
    attrs.to = "/about"
    child(CouplingButton(supersize, orange, "", {}, {}, fun RBuilder.() {
 +"About Coupling"
}))
}

private fun RBuilder.newTribeButton(className: String) = Link {
    attrs.to = "/new-tribe/"
    child(CouplingButton(supersize, green, className, {}, {}, fun RBuilder.() {
 +"Add a new tribe!"
}))
}
