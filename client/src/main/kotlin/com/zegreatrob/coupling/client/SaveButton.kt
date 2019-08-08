package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.ComponentProvider
import com.zegreatrob.coupling.client.external.react.StyledComponentBuilder
import com.zegreatrob.coupling.client.external.react.buildBy
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import react.RBuilder
import react.RProps
import react.dom.a

object SaveButton : ComponentProvider<SaveButtonProps>(), SaveButtonBuilder

val RBuilder.saveButton get() = SaveButton.captor(this)

data class SaveButtonProps(
    val onClickFunction: (Event) -> Unit
) : RProps

external class SaveButtonStyles {
    val className: String
}

interface SaveButtonBuilder : StyledComponentBuilder<SaveButtonProps, SaveButtonStyles> {

    override val componentPath: String get() = "SaveButton"

    override fun build() = buildBy {
        {
            a(classes = "super green button") {
                attrs {
                    classes += styles.className
                    onClickFunction = props.onClickFunction
                }
                +"Save!"
            }
        }
    }

}
