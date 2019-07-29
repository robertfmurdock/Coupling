package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.ComponentProvider
import com.zegreatrob.coupling.client.StyledComponentBuilder
import com.zegreatrob.coupling.client.buildBy
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import kotlinx.html.classes
import react.RProps
import react.dom.a
import react.dom.div

object TribeList : ComponentProvider<TribeListProps>(), TribeListBuilder

data class TribeListProps(val tribes: List<KtTribe>, val pathSetter: (String) -> Unit) : RProps

interface TribeListCss {
    val className: String
    val newTribeButton: String
}

interface TribeListBuilder : StyledComponentBuilder<TribeListProps, TribeListCss> {

    override val componentPath: String get() = "tribe/TribeList"

    override fun build() = buildBy {
        val (tribes, pathSetter) = props
        {
            div(classes = styles.className) {
                div {
                    tribes.forEach { tribe ->
                        tribeCard(TribeCardProps(tribe, pathSetter = pathSetter), key = tribe.id.value)
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
    }
}
