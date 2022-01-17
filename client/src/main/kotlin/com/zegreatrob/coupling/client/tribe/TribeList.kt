package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.client.dom.*
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import kotlinx.css.*
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.span
import react.router.dom.Link

data class TribeList(val tribes: List<Tribe>) : DataProps<TribeList> {
    override val component: TMFC<TribeList> get() = tribeList
}

private val styles = useStyles("tribe/TribeList")

val tribeList = tmFC<TribeList> { (tribes) ->
    div {
        className = styles.className
        cssDiv(css = {
            display = Display.flex
        }) {
            cssSpan(css = {
                margin(10.px)
                display = Display.flex
                alignItems = Align.center
            }) {
                img {
                    src = svgPath("logo")
                    width = 72.0
                    height = 48.0
                }
            }
            cssH1(css = { flexGrow = 2.0; display = Display.inlineBlock }) {
                cssDiv(css = {
                    display = Display.flex
                    alignItems = Align.center
                }) {
                    cssSpan(css = { flexGrow = 2.0; textAlign = TextAlign.left }) { +"Tribe List" }
                    span {
                        aboutButton()
                        GqlButton()
                    }
                }
            }
        }
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
    child(CouplingButton(large, orange, "")) {
        cssSpan(css = { margin(2.px) }) {
            img {
                src = svgPath("logo")
                width = 27.0
                height = 18.0
            }
        }
        span { +"About" }
    }
}

private fun ChildrenBuilder.newTribeButton(className: String) = Link {
    to = "/new-tribe/"
    child(CouplingButton(supersize, green, className)) { +"Add a new tribe!" }
}
