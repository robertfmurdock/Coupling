package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.*
import com.zegreatrob.coupling.client.dom.*
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import kotlinx.css.*
import react.ChildrenBuilder
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.dom.svg.ReactSVG
import react.router.dom.Link

data class TribeList(val tribes: List<Tribe>) : DataProps<TribeList> {
    override val component: TMFC<TribeList> get() = tribeList
}

private val styles = useStyles("tribe/TribeList")

val tribeList = tmFC<TribeList> { (tribes) ->
    div {
        className = styles.className
        cssDiv(css = {
            display = Display.flex
            backgroundColor = Color("#faf0d2")
            borderRadius = 50.px
        }) {
            cssSpan(css = {
                margin(10.px)
                display = Display.flex
                alignItems = Align.center
            }) {
                proportionateSvg(72.0, 48.0, 36.0, 24.0, svgPath("logo"))
            }
            cssH1(css = { flexGrow = 2.0; display = Display.inlineBlock }) {
                cssDiv(css = {
                    display = Display.flex
                    alignItems = Align.center
                }) {
                    cssSpan(css = { flexGrow = 2.0; textAlign = TextAlign.left }) {
                        +"Tribe List"
                    }
                    span {
                        AboutButton()
                        LogoutButton()
                        GqlButton()
                        NotificationButton()
                    }
                }
            }
        }
        div {
            tribes.forEach { tribe ->
                child(TribeCard(tribe), key = tribe.id.value)
            }
        }
        div { newTribeButton(styles["newTribeButton"]) }
    }
}

private fun ChildrenBuilder.proportionateSvg(
    width: Double,
    height: Double,
    originalWidth: Double,
    originalHeight: Double,
    href: String
) {
    ReactSVG.svg {
        fill = "none"
        viewBox = "0 0 $originalWidth $originalHeight"
        this.width = width
        this.height = height
        ReactSVG.image {
            this.href = href
            preserveAspectRatio = "true"
        }
    }
}

private val AboutButton = FC<Props> {
    Link {
        to = "/about"
        tabIndex = -1
        draggable = false
        child(CouplingButton(large, orange, "")) {
            span { +"About" }
            cssSpan(css = { margin(2.px) }) {
                proportionateSvg(27.0, 18.0, 36.0, 24.0, svgPath("logo"))
            }
        }
    }
}

private fun ChildrenBuilder.newTribeButton(className: String) = Link {
    to = "/new-tribe/"
    tabIndex = -1
    draggable = false
    child(CouplingButton(supersize, green, className)) { +"Add a new tribe!" }
}
