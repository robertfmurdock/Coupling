package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.minreact.EmptyProps
import react.PropsWithChildren
import react.RBuilder
import react.RHandler
import react.dom.ul
import react.fc

fun RBuilder.editor(handler: RHandler<EmptyProps>) = child(Editor, handler = handler)

private val styles = useStyles("Editor")

val Editor = fc<PropsWithChildren> { props ->
    ul(classes = styles.className) {
        props.children()
    }
}
