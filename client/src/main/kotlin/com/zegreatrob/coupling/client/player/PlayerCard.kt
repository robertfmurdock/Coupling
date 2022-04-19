package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.cssDiv
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.gravatar.GravatarOptions
import com.zegreatrob.coupling.client.gravatar.gravatarImage
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import csstype.ClassName
import kotlinx.css.Color
import kotlinx.css.RuleSet
import kotlinx.css.borderRadius
import kotlinx.css.borderWidth
import kotlinx.css.flex
import kotlinx.css.height
import kotlinx.css.margin
import kotlinx.css.properties.Angle
import kotlinx.css.properties.boxShadow
import kotlinx.css.properties.deg
import kotlinx.css.properties.rotate
import kotlinx.css.properties.s
import kotlinx.css.properties.transform
import kotlinx.css.properties.transition
import kotlinx.css.px
import kotlinx.css.width
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import react.ChildrenBuilder
import react.dom.html.ReactHTML.img

data class PlayerCard(
    val player: Player,
    val className: ClassName? = null,
    val size: Int = 100,
    val onClick: (Event) -> Unit = {},
    val deselected: Boolean = false,
    val tilt: Angle = 0.deg
) : DataPropsBind<PlayerCard>(playerCard)

private val styles = useStyles("player/PlayerCard")

val playerCard = tmFC<PlayerCard> { (player, className, size, onClick, deselected, tilt) ->
    cssDiv(
        attrs = {
            classes = classes + additionalClasses(className, deselected)
            onClickFunction = onClick
        },
        css = {
            transition(duration = 0.25.s)
            transform {
                rotate(tilt)
            }
            playerCardRuleSet(size)()
        }
    ) {
        cssDiv(css = { margin((size * 0.02).px) }) {
            playerGravatarImage(player, size)
            child(PlayerCardHeader(player, size))
        }
    }
}

private fun additionalClasses(className: ClassName?, deselected: Boolean) = setOf(className, styles["player"])
    .mapNotNull { it?.toString() }
    .let {
        when {
            deselected -> it + "${styles["deselected"]}"
            else -> it
        }
    }

private fun playerCardRuleSet(size: Int): RuleSet = {
    val totalExtraMarginNeededForImage = 2 * (size * 0.02)
    width = (size + totalExtraMarginNeededForImage).px
    height = (size * 1.4).px
    borderWidth = (size * 0.04).px
    borderRadius = (size * 0.08).px
    boxShadow(Color("rgba(0, 0, 0, 0.6)"), (size * 0.02).px, (size * 0.04).px, (size * 0.04).px)
    flex(0.0, 0.0)
}

private fun ChildrenBuilder.playerGravatarImage(player: Player, size: Int) = if (player.imageURL != null) {
    img {
        this.src = player.imageURL
        className = styles["playerIcon"]
        alt = "icon"
        this.width = size.toDouble()
        this.height = size.toDouble()
    }
} else {
    gravatarImage(
        email = player.emailWithFallback(),
        className = styles["playerIcon"],
        alt = "icon",
        options = object : GravatarOptions {
            override val size = size
            override val default = "retro"
        }
    )
}

private fun Player.emailWithFallback() = when {
    email != "" -> email
    name != "" -> name
    else -> "name"
}
