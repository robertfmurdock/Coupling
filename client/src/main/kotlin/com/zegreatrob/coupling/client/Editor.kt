package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.EmptyProps
import com.zegreatrob.coupling.client.external.react.reactFunction2
import com.zegreatrob.coupling.client.external.react.useStyles
import react.RBuilder
import react.RHandler
import react.dom.ul

fun RBuilder.editor(handler: RHandler<EmptyProps>) = child(Editor, EmptyProps, handler = handler)

private val styles = useStyles("Editor")

val Editor = reactFunction2<EmptyProps> { props ->
    ul(classes = styles.className) {
        props.children()
    }
}
