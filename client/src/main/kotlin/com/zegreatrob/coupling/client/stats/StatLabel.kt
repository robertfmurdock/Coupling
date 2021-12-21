package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.useStyles
import react.FC
import react.PropsWithChildren
import react.dom.html.ReactHTML.span

private val styles = useStyles("stats/StatLabel")

val StatLabel = FC<PropsWithChildren> { props ->
    span {
        className = styles.className
        props.children()
    }
}
