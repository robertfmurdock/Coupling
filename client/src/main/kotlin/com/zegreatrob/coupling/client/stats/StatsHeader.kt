package com.zegreatrob.coupling.client.stats

import com.zegreatrob.coupling.client.external.react.useStyles
import react.FC
import react.PropsWithChildren
import react.dom.html.ReactHTML.div

private val styles = useStyles("stats/StatsHeader")

val StatsHeader = FC<PropsWithChildren> { props ->
    div {
        className = styles.className
        props.children()
    }
}
