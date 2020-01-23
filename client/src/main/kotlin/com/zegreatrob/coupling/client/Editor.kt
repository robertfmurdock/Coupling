package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.*
import react.RBuilder
import react.RHandler
import react.dom.ul

external interface EditorStyles {
    val className: String
}

object Editor : FRComponent<EmptyProps>(provider()) {
    override fun render(props: EmptyProps) = reactElement {
        val styles = useStyles<EditorStyles>("Editor")
        ul(classes = styles.className) {
            props.children()
        }
    }

    fun RBuilder.editor(handler: RHandler<EmptyProps>) = child(Editor(EmptyProps, handler = handler))
}