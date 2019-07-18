package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import kotlinx.css.*
import kotlinx.html.SPAN
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import kotlinx.html.tabIndex
import loadStyles
import org.w3c.dom.Node
import org.w3c.dom.events.Event
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

private val styles: TribeCardStyles = loadStyles("tribe/TribeCard")

data class TribeCardProps(val tribe: KtTribe, val size: Int = 150, val pathSetter: (String) -> Unit) : RProps

interface TribeCardRenderer {

    companion object {
        val tribeCard = reactFunctionComponent { props: TribeCardProps ->
            val (tribe, size) = props

            styledSpan {
                attrs {
                    classes = setOf(styles.className)
                    onClickFunction = { props.goToPairAssignments() }
                    tabIndex = "0"
                    tribeCardCss(size)
                }
                tribeCardHeader(props)
                tribeGravatar(tribe, size)
            }
        }

        private fun TribeCardProps.goToPairAssignments() = pathSetter("/${tribe.id.value}/pairAssignments/current/")

        private fun StyledDOMBuilder<SPAN>.tribeCardCss(size: Int) = css {
            width = size.px
            height = (size * 1.4).px
            padding((size * 0.02).px)
            borderWidth = (size * 0.01).px
        }

        private fun StyledDOMBuilder<SPAN>.tribeCardHeader(props: TribeCardProps) = with(props) {
            val tribeNameRef = useRef(null)
            useLayoutEffect { tribeNameRef.current?.fitTribeName(size) }

            styledDiv {
                attrs {
                    classes = setOf(styles.header)
                    css {
                        margin((size * 0.02).px, 0.px, 0.px, 0.px)
                        height = (size * 0.35).px
                    }
                    onClickFunction = { event -> goToConfigTribe(event) }
                }
                div {
                    attrs { ref = tribeNameRef }
                    +(tribe.name ?: "Unknown")
                }
            }
        }

        private fun TribeCardProps.goToConfigTribe(event: Event) {
            event.stopPropagation(); pathSetter("/${tribe.id.value}/edit/")
        }

        private fun Node.fitTribeName(size: Int) = fitHeaderNode(
                maxFontHeight = (size * 0.3),
                minFontHeight = (size * 0.16)
        )

        private fun StyledDOMBuilder<SPAN>.tribeGravatar(tribe: KtTribe, size: Int) = gravatarImage(
                email = tribe.email,
                alt = "tribe-img",
                fallback = "/images/icons/tribes/no-tribe.png",
                options = object : GravatarOptions {
                    override val size = size
                    override val default = "identicon"
                }
        )

    }
}

