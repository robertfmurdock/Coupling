package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.fitty.fitty
import com.zegreatrob.coupling.client.tribeConfig
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction
import kotlinx.css.height
import kotlinx.css.margin
import kotlinx.css.px
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import org.w3c.dom.Node
import org.w3c.dom.events.Event
import react.*
import react.router.dom.redirect
import styled.css
import styled.styledDiv

private val styles = useStyles("tribe/TribeCard")

fun RBuilder.tribeCardHeader(tribe: Tribe, size: Int) = child(tribeCardHeader, TribeCardHeaderProps(tribe, size))

data class TribeCardHeaderProps(val tribe: Tribe, val size: Int) : RProps

val tribeCardHeader = reactFunction<TribeCardHeaderProps> { (tribe, size) ->
    val tribeNameRef = useRef<Node?>(null)
    useLayoutEffect { tribeNameRef.current?.fitTribeName(size) }

    val (redirectUrl, setRedirectUrl) = useState<String?>(null)
    val onClick = { event: Event -> goToConfigTribe(event, setRedirectUrl, tribe) }

    styledDiv {
        attrs {
            ref = tribeNameRef
            classes = setOf(styles["header"])
            css {
                margin((size * 0.02).px, 0.px, 0.px, 0.px)
                height = (size * 0.35).px
            }
            onClickFunction = onClick
        }
        +(tribe.name ?: "Unknown")
        redirectUrl?.let { redirect(to = it) }
    }
}

private fun goToConfigTribe(event: Event, pathSetter: (String) -> Unit, tribe: Tribe) {
    event.stopPropagation()
    pathSetter.tribeConfig(tribe)
}

private fun Node.fitTribeName(size: Int) = fitty(
    maxFontHeight = (size * 0.3),
    minFontHeight = (size * 0.16),
    multiLine = true
)