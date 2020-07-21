package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.minreact.EmptyProps
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction
import react.RBuilder
import react.RHandler
import react.dom.ul

fun RBuilder.editor(handler: RHandler<EmptyProps>) = child(Editor, handler = handler)

private val styles = useStyles("Editor")

val Editor = reactFunction<EmptyProps> { props ->
    ul(classes = styles.className) {
        props.children()
    }
}
