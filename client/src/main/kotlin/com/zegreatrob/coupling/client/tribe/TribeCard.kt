package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.fitty.fitty
import com.zegreatrob.coupling.client.gravatar.GravatarOptions
import com.zegreatrob.coupling.client.gravatar.gravatarImage
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import kotlinx.css.*
import kotlinx.html.SPAN
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import kotlinx.html.tabIndex
import org.w3c.dom.Node
import org.w3c.dom.events.Event
import react.RBuilder
import react.RProps
import react.ReactElement
import react.dom.div
import styled.StyledDOMBuilder
import styled.css
import styled.styledDiv
import styled.styledSpan

object TribeCard : ComponentProvider<TribeCardProps>(provider()), TribeCardBuilder

val RBuilder.tribeCard get() = TribeCard.render(this)

data class TribeCardProps(val tribe: KtTribe, val size: Int = 150, val pathSetter: (String) -> Unit) : RProps

external interface TribeCardStyles {
    val className: String
    val header: String

}

interface TribeCardBuilder : StyledComponentRenderer<TribeCardProps, TribeCardStyles> {

    override val componentPath: String get() = "tribe/TribeCard"

    override fun StyledRContext<TribeCardProps, TribeCardStyles>.render(): ReactElement {
        val (tribe, size) = props

        return reactElement {
            styledSpan {
                attrs {
                    classes = setOf(styles.className)
                    onClickFunction = { props.goToPairAssignments() }
                    tabIndex = "0"
                    tribeCardCss(size)
                }
                tribeCardHeader(props, styles)
                tribeGravatar(tribe, size)
            }
        }
    }

    private fun TribeCardProps.goToPairAssignments() = pathSetter("/${tribe.id.value}/pairAssignments/current/")

    private fun StyledDOMBuilder<SPAN>.tribeCardCss(size: Int) = css {
        width = size.px
        height = (size * 1.4).px
        padding((size * 0.02).px)
        borderWidth = (size * 0.01).px
    }

    private fun StyledDOMBuilder<SPAN>.tribeCardHeader(props: TribeCardProps, styles: TribeCardStyles) = with(props) {
        val tribeNameRef = useRef<Node>(null)
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

    private fun Node.fitTribeName(size: Int) = fitty(
        maxFontHeight = (size * 0.3),
        minFontHeight = (size * 0.16),
        multiLine = true
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
