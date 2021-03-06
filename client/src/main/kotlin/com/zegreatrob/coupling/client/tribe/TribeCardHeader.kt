package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.Paths.tribeConfigPath
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.fitty.fitty
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction
import kotlinx.css.height
import kotlinx.css.margin
import kotlinx.css.px
import kotlinx.html.classes
import org.w3c.dom.Node
import react.RBuilder
import react.RProps
import react.dom.attrs
import react.router.dom.routeLink
import react.useLayoutEffect
import react.useRef
import styled.css
import styled.styledDiv

private val styles = useStyles("tribe/TribeCard")

fun RBuilder.tribeCardHeader(tribe: Tribe, size: Int) = child(tribeCardHeader, TribeCardHeaderProps(tribe, size))

data class TribeCardHeaderProps(val tribe: Tribe, val size: Int) : RProps

val tribeCardHeader = reactFunction<TribeCardHeaderProps> { (tribe, size) ->
    val tribeNameRef = useRef<Node>(null)
    useLayoutEffect { tribeNameRef.current?.fitTribeName(size) }
    styledDiv {
        attrs {
            ref = tribeNameRef
            classes = setOf(styles["header"])
            css {
                margin((size * 0.02).px, 0.px, 0.px, 0.px)
                height = (size * 0.35).px
            }
        }
        routeLink(tribe.tribeConfigPath()) {
            +(tribe.name ?: "Unknown")
        }
    }
}

private fun Node.fitTribeName(size: Int) = fitty(
    maxFontHeight = (size * 0.3),
    minFontHeight = (size * 0.16),
    multiLine = true
)