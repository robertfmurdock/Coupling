package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.external.react.render
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.fitty.fitty
import com.zegreatrob.coupling.client.gravatar.GravatarOptions
import com.zegreatrob.coupling.client.gravatar.gravatarImage
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.css.*
import kotlinx.html.SPAN
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import kotlinx.html.tabIndex
import org.w3c.dom.Node
import org.w3c.dom.events.Event
import react.RBuilder
import react.RProps
import react.useLayoutEffect
import react.useRef
import styled.StyledDOMBuilder
import styled.css
import styled.styledDiv
import styled.styledSpan

data class TribeCardProps(val tribe: Tribe, val size: Int = 150, val pathSetter: (String) -> Unit) : RProps

val RBuilder.tribeCard get() = TribeCard.render(this)

private val styles = useStyles("tribe/TribeCard")

val TribeCard = reactFunction<TribeCardProps> { props ->
    val (tribe, size) = props
    styledSpan {
        attrs {
            classes = setOf(styles.className)
            onClickFunction = { goToPairAssignments(props.pathSetter, props.tribe.id) }
            tabIndex = "0"
            tribeCardCss(size)
            setProp("data-tribe-id", tribe.id.value)
        }
        tribeCardHeader(props)
        tribeGravatar(tribe, size)
    }
}

private fun goToPairAssignments(pathSetter: (String) -> Unit, tribeId: TribeId) =
    pathSetter("/${tribeId.value}/pairAssignments/current/")

private fun StyledDOMBuilder<SPAN>.tribeCardCss(size: Int) = css {
    width = size.px
    height = (size * 1.4).px
    padding((size * 0.02).px)
    borderWidth = (size * 0.01).px
}

private fun RBuilder.tribeCardHeader(props: TribeCardProps) = with(props) {
    val tribeNameRef = useRef<Node?>(null)
    useLayoutEffect { tribeNameRef.current?.fitTribeName(size) }

    styledDiv {
        attrs {
            ref = tribeNameRef
            classes = setOf(styles["header"])
            css {
                margin((size * 0.02).px, 0.px, 0.px, 0.px)
                height = (size * 0.35).px
            }
            onClickFunction = { event -> goToConfigTribe(event) }
        }
        +(tribe.name ?: "Unknown")
    }
}

private fun TribeCardProps.goToConfigTribe(event: Event) {
    event.stopPropagation(); pathSetter("/${tribe.id.value}/edit/")
}

private fun Node.fitTribeName(size: Int) = fitty(
    maxFontHeight = (size * 0.3),
    minFontHeight = (size * 0.16),
    multiLine = true
)

private fun StyledDOMBuilder<SPAN>.tribeGravatar(tribe: Tribe, size: Int) = gravatarImage(
    email = tribe.email,
    alt = "tribe-img",
    fallback = "/images/icons/tribes/no-tribe.png",
    options = object : GravatarOptions {
        override val size = size
        override val default = "identicon"
    }
)
