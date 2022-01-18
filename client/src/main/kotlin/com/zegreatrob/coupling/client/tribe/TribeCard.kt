package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.Paths.currentPairsPage
import com.zegreatrob.coupling.client.cssDiv
import com.zegreatrob.coupling.client.cssSpan
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.gravatar.GravatarOptions
import com.zegreatrob.coupling.client.gravatar.gravatarImage
import com.zegreatrob.coupling.client.pngPath
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import kotlinx.css.*
import kotlinx.html.classes
import kotlinx.html.tabIndex
import react.ChildrenBuilder
import react.router.dom.Link

data class TribeCard(val tribe: Tribe, val size: Int = 150) : DataProps<TribeCard> {
    override val component: TMFC<TribeCard> = tribeCard
}

private val styles = useStyles("tribe/TribeCard")

val tribeCard = tmFC<TribeCard> { (tribe, size) ->
    Link {
        to = tribe.id.currentPairsPage()
        cssSpan(
            attrs = {
                classes = classes + styles.className
                tabIndex = "0"
                attributes["data-tribe-id"] = tribe.id.value
            },
            css = tribeCardCss(size)
        ) {
            cssDiv(css = { margin((size * 0.02).px) }) {
                child(TribeCardHeader(tribe, size))
                tribeGravatar(tribe, size)
            }
        }
    }
}

private fun tribeCardCss(size: Int): RuleSet = {
    val totalExtraMarginNeededForImage = 2 * (size * 0.02)
    width = (size + totalExtraMarginNeededForImage).px
    height = (size * 1.4).px
    borderWidth = (size * 0.01).px
    borderRadius = (size * 10.0 / 150).px
}


val noTribeImagePath = pngPath("tribes/no-tribe")

private fun ChildrenBuilder.tribeGravatar(tribe: Tribe, size: Int) = gravatarImage(
    email = tribe.email,
    alt = "tribe-img",
    fallback = noTribeImagePath,
    options = object : GravatarOptions {
        override val size = size
        override val default = "identicon"
    }
)
