package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.green
import com.zegreatrob.coupling.client.dom.orange
import com.zegreatrob.coupling.client.dom.supersize
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div
import react.router.dom.Link

data class TribeList(val tribes: List<Tribe>) : DataProps<TribeList> {
    override val component: TMFC<TribeList> get() = tribeList
}

private val styles = useStyles("tribe/TribeList")

val tribeList = tmFC<TribeList> { (tribes) ->
    div {
        className = styles.className
        div { aboutButton() }
        div {
            tribes.forEach { tribe ->
                child(TribeCard(tribe), key = tribe.id.value)
            }
        }
        div { newTribeButton(styles["newTribeButton"]) }
    }
}

private fun ChildrenBuilder.aboutButton() = Link {
    to = "/about"
    child(CouplingButton(supersize, orange, "", {}, {}) { +"About Coupling" })
}

private fun ChildrenBuilder.newTribeButton(className: String) = Link {
    to = "/new-tribe/"
    child(CouplingButton(supersize, green, className, {}, {}) { +"Add a new tribe!" })
}
