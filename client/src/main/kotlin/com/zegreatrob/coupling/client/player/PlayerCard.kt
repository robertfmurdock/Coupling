package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.gravatar.GravatarOptions
import com.zegreatrob.coupling.client.gravatar.gravatarImage
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import kotlinx.css.*
import kotlinx.css.properties.*
import kotlinx.html.DIV
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import react.create
import react.dom.attrs
import react.dom.html.ReactHTML.img
import styled.StyledDOMBuilder
import styled.css
import styled.styledDiv

data class PlayerCard(
    val tribeId: TribeId,
    val player: Player,
    val linkToConfig: Boolean = false,
    val className: String? = null,
    val size: Int = 100,
    val onClick: ((Event) -> Unit) = {},
    val deselected: Boolean = false,
    val tilt: Angle = 0.deg
) : DataProps<PlayerCard> {
    override val component: TMFC<PlayerCard> get() = playerCard
}

private val styles = useStyles("player/PlayerCard")

val playerCard = reactFunction<PlayerCard> { props ->
    val (tribeId, player, linkToConfig, className, size, onClick, deselected, tilt) = props
    styledDiv {
        attrs {
            classes = classes + additionalClasses(className, deselected)
            playerCardStyle(size)
            onClickFunction = onClick
        }
        css {
            transition(duration = 0.25.s)
            transform {
                rotate(tilt)
            }
        }

        child(playerGravatarImage(player, size))
        child(PlayerCardHeader(tribeId, player, linkToConfig, size))
    }
}

private fun additionalClasses(className: String?, deselected: Boolean) = setOf(className, styles["player"])
    .filterNotNull()
    .let {
        when {
            deselected -> it + styles["deselected"]
            else -> it
        }
    }

private fun StyledDOMBuilder<DIV>.playerCardStyle(size: Int) = css {
    width = size.px
    height = (size * 1.4).px
    padding(all = (size * 0.06).px)
    borderWidth = (size * 0.04).px
    borderRadius = (size * 0.08).px
    boxShadow(Color("rgba(0, 0, 0, 0.6)"), (size * 0.02).px, (size * 0.04).px, (size * 0.04).px)
}

private fun playerGravatarImage(player: Player, size: Int) = if (player.imageURL != null) {
    img.create {
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

