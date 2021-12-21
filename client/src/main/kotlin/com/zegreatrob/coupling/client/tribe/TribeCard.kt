package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.Paths.currentPairsPage
import com.zegreatrob.coupling.client.external.react.childCurry
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.gravatar.GravatarOptions
import com.zegreatrob.coupling.client.gravatar.gravatarImage
import com.zegreatrob.coupling.client.pngPath
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataProps
import kotlinx.css.*
import kotlinx.html.SPAN
import kotlinx.html.classes
import kotlinx.html.tabIndex
import react.RBuilder
import react.dom.attrs
import react.dom.setProp
import react.router.dom.Link
import styled.StyledDOMBuilder
import styled.css
import styled.styledSpan

data class TribeCardProps(val tribe: Tribe, val size: Int = 150) : DataProps

val RBuilder.tribeCard get() = childCurry(TribeCard)

private val styles = useStyles("tribe/TribeCard")

val TribeCard = reactFunction<TribeCardProps> { (tribe, size) ->
    Link {
        attrs.to = tribe.id.currentPairsPage()
        styledSpan {
            attrs {
                tribeCardCss(size)
                classes = classes + styles.className
                tabIndex = "0"
                setProp("data-tribe-id", tribe.id.value)
            }
            tribeCardHeader(tribe, size)
            tribeGravatar(tribe, size)
        }
    }
}

private fun StyledDOMBuilder<SPAN>.tribeCardCss(size: Int) = css {
    width = size.px
    height = (size * 1.4).px
    padding((size * 0.02).px)
    borderWidth = (size * 0.01).px
}


val noTribeImagePath = pngPath("tribes/no-tribe")

private fun RBuilder.tribeGravatar(tribe: Tribe, size: Int) = gravatarImage(
    email = tribe.email,
    alt = "tribe-img",
    fallback = noTribeImagePath,
    options = object : GravatarOptions {
        override val size = size
        override val default = "identicon"
    }
)
