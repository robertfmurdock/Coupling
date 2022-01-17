package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.Paths.tribeConfigPath
import com.zegreatrob.coupling.client.cssDiv
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.fitty.fitty
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.tmFC
import kotlinx.css.*
import kotlinx.html.classes
import org.w3c.dom.Node
import react.ref
import react.router.dom.Link
import react.useLayoutEffect
import react.useRef

private val styles = useStyles("tribe/TribeCard")

data class TribeCardHeader(val tribe: Tribe, val size: Int) : DataProps<TribeCardHeader> {
    override val component: TMFC<TribeCardHeader> get() = tribeCardHeader
}

val tribeCardHeader = tmFC<TribeCardHeader> { (tribe, size) ->
    val tribeNameRef = useRef<Node>(null)
    useLayoutEffect { tribeNameRef.current?.fitTribeName(size) }
    cssDiv(
        attrs = { classes = setOf(styles["header"]) },
        css = {
            margin((size * 0.02).px, 0.px, 0.px, 0.px)
            height = (size * 0.33).px
            borderRadius = (size / 10).px
            overflow = Overflow.hidden
        }
    ) {
        cssDiv(
            props = { ref = tribeNameRef },
            css = {
                height = (size * 0.33).px
            }
        ) {
            cssDiv(css = {
                display = Display.flex
                alignItems = Align.center
                height = (size * 0.33).px
            }) {
                Link {
                    to = tribe.tribeConfigPath()
                    +(tribe.name ?: "Unknown")
                }
            }
        }
    }
}

private fun Node.fitTribeName(size: Int) = fitty(
    maxFontHeight = (size * 0.3),
    minFontHeight = (size * 0.10),
    multiLine = true
)
