package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import kotlinx.html.classes
import loadStyles
import react.RProps
import react.dom.a
import react.dom.div

data class TribeListProps(val tribes: List<KtTribe>, val pathSetter: (String) -> Unit) : RProps

interface TribeListCss {
    val className: String
    val newTribeButton: String
}

private val styles = loadStyles<TribeListCss>("TribeList")

val tribeList = rFunction { props: TribeListProps ->
    val (tribes, pathSetter) = props

    div(classes = styles.className) {
        div {
            tribes.forEach { tribe ->
                element(tribeCard, TribeCardProps(tribe = tribe, pathSetter = pathSetter), key = tribe.id.value)
            }
        }
        div {
            a(href = "/new-tribe/", classes = "super green button") {
                attrs {
                    classes += styles.newTribeButton
                    type = "button"
                }
                +"Add a new tribe!"
            }
        }
    }
}
