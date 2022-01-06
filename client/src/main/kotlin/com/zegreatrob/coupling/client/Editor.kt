package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.useStyles
import react.FC
import react.PropsWithChildren
import react.dom.html.ReactHTML.ul

private val styles = useStyles("Editor")

val Editor = FC<PropsWithChildren> { props ->
    ul {
        className = styles.className
        props.children()
    }
}
