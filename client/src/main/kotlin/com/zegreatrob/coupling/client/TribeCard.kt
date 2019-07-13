package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import kotlinx.css.*
import kotlinx.html.SPAN
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import kotlinx.html.tabIndex
import loadStyles
import org.w3c.dom.Node
import react.RProps
import react.dom.div
import styled.StyledDOMBuilder
import styled.css
import styled.styledDiv
import styled.styledSpan

private external interface TribeCardStyles {
    val className: String
    val header: String
}

private val styles: TribeCardStyles = loadStyles("TribeCard")

data class TribeCardProps(val tribe: KtTribe, val size: Int = 150, val pathSetter: (String) -> Unit) : RProps

val tribeCard = rFunction { props: TribeCardProps ->
    val (tribe, size) = props

    styledSpan {
        attrs {
            classes = setOf("tribe-card", styles.className)
            onClickFunction = { props.goToPairAssignments() }
            tabIndex = "0"
            tribeCardCss(size)
        }
        tribeCardHeader(props)
        tribeGravatar(tribe, size)
    }
}

private fun TribeCardProps.goToPairAssignments() {
    pathSetter("/${tribe.id.value}/pairAssignments/current/")
}

private fun StyledDOMBuilder<SPAN>.tribeGravatar(tribe: KtTribe, size: Int) {
    gravatarImage(
            email = tribe.email,
            alt = "tribe-img",
            fallback = "/images/icons/tribes/no-tribe.png",
            options = object : GravatarOptions {
                override val size = size
                override val default = "identicon"
            }
    )
}

private fun StyledDOMBuilder<SPAN>.tribeCardHeader(props: TribeCardProps) = with(props) {
    val tribeNameRef = useRef(null)
    useLayoutEffect { tribeNameRef.current?.fitTribeName(size) }

    styledDiv {
        attrs {
            classes = setOf("tribe-card-header")
            css {
                margin((size * 0.02).px, 0.px, 0.px, 0.px)
                height = (size * 0.35).px
            }
        }
        div {
            attrs {
                ref = tribeNameRef
                classes = setOf(styles.header)
                onClickFunction = { event -> event.stopPropagation(); pathSetter("/${tribe.id.value}/edit/") }
            }
            div {
                +(tribe.name ?: "Unknown")
            }
        }
    }
}

private fun Node.fitTribeName(size: Int) {
    val maxFontHeight = (size * 0.3)
    val minFontHeight = (size * 0.16)
    fitHeaderNode(maxFontHeight, minFontHeight)
}

private fun StyledDOMBuilder<SPAN>.tribeCardCss(size: Int) {
    css {
        width = size.px
        height = (size * 1.4).px
        padding((size * 0.02).px)
        borderWidth = (size * 0.01).px
    }
}
