package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.gravatar.GravatarOptions
import com.zegreatrob.coupling.client.gravatar.gravatarImage
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.create
import com.zegreatrob.minreact.tmFC
import csstype.Angle
import csstype.Auto
import csstype.BoxShadow
import csstype.ClassName
import csstype.Color
import csstype.Flex
import csstype.PropertiesBuilder
import csstype.deg
import csstype.number
import csstype.px
import csstype.rotate
import csstype.s
import emotion.react.css
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img

data class PlayerCard(
    val player: Player,
    val className: ClassName? = null,
    val size: Int = 100,
    val onClick: () -> Unit = {},
    val deselected: Boolean = false,
    val tilt: Angle = 0.deg
) : DataPropsBind<PlayerCard>(playerCard)

private val styles = useStyles("player/PlayerCard")

val playerCard = tmFC<PlayerCard> { (player, className, size, onClick, deselected, tilt) ->
    div {
        css(classNames = additionalClasses(className, deselected)) {
            transitionDuration = 0.25.s
            transform = rotate(tilt)
            playerCardRuleSet(size)()
        }
        this.onClick = { onClick() }
        div {
            css { margin = ((size * 0.02).px) }
            playerGravatarImage(player, size)
            +PlayerCardHeader(player, size).create()
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
    }.map(::ClassName)
    .toTypedArray()

private fun playerCardRuleSet(size: Int): PropertiesBuilder.() -> Unit = {
    val totalExtraMarginNeededForImage = 2 * (size * 0.02)
    width = (size + totalExtraMarginNeededForImage).px
    height = (size * 1.4).px
    borderWidth = (size * 0.04).px
    borderRadius = (size * 0.08).px
    boxShadow = BoxShadow((size * 0.02).px, (size * 0.04).px, (size * 0.04).px, Color("rgba(0, 0, 0, 0.6)"))
    flex = Flex(number(0.0), number(0.0), Auto.auto)
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
