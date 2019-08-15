package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import kotlinx.html.classes
import react.RProps
import react.ReactElement
import react.dom.a
import react.dom.div

object TribeList : ComponentProvider<TribeListProps>(), TribeListBuilder

data class TribeListProps(val tribes: List<KtTribe>, val pathSetter: (String) -> Unit) : RProps

interface TribeListCss {
    val className: String
    val newTribeButton: String
}

interface TribeListBuilder : StyledComponentBuilder<TribeListProps, TribeListCss>,
    StyledComponentRenderer<TribeListProps, TribeListCss> {

    override fun build() = functionFromRender()

    override val componentPath: String get() = "tribe/TribeList"

    override fun PropsStylesBuilder<TribeListProps, TribeListCss>.render(): ReactElement {
        val (tribes, pathSetter) = props
        return reactElement {
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
