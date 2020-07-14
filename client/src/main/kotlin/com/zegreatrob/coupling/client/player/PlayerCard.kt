package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.external.react.render
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.fitty.fitty
import com.zegreatrob.coupling.client.gravatar.GravatarOptions
import com.zegreatrob.coupling.client.gravatar.gravatarImage
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.css.*
import kotlinx.html.DIV
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import org.w3c.dom.Node
import org.w3c.dom.events.Event
import react.*
import react.dom.div
import react.dom.img
import styled.StyledDOMBuilder
import styled.css
import styled.styledDiv

typealias PathSetter = (String) -> Unit

val RBuilder.playerCard get() = PlayerCard.render(this)

data class PlayerCardProps(
    val tribeId: TribeId,
    val player: Player,
    val pathSetter: PathSetter? = null,
    val className: String? = null,
    val size: Int = 100,
    val onClick: ((Event) -> Unit) = {},
    val deselected: Boolean = false
) : RProps

private val styles = useStyles("player/PlayerCard")

val PlayerCard = reactFunction<PlayerCardProps> { props ->
    val (tribeId, player, pathSetter, className, size, onClick, deselected) = props
    styledDiv {
        attrs {
            classes += additionalClasses(className, deselected)
            playerCardStyle(size)
            onClickFunction = onClick
        }
        playerGravatarImage(player, size)
        child(playerCardHeaderElement(tribeId, player, pathSetter, size))
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
}

private fun playerCardHeaderElement(
    tribeId: TribeId,
    player: Player,
    pathSetter: PathSetter?,
    size: Int
) = buildElement {
    val playerNameRef = useRef<Node?>(null)
    useLayoutEffect { playerNameRef.current?.fitPlayerName(size) }

    styledDiv {
        attrs {
            classes += styles["header"]
            onClickFunction = handleNameClick(tribeId, player, pathSetter)
        }
        css { margin(top = (size * 0.02).px) }
        div {
            attrs { ref = playerNameRef }
            +(if (player.name.isBlank()) "Unknown" else player.name)
        }
    }
}

private fun handleNameClick(tribeId: TribeId, player: Player, pathSetter: PathSetter?) =
    { event: Event ->
        if (pathSetter != null) {
            event.stopPropagation()

            pathSetter("/${tribeId.value}/player/${player.id}/")
        }
    }

private fun RBuilder.playerGravatarImage(player: Player, size: Int) = if (player.imageURL != null) {
    img(src = player.imageURL, classes = styles["playerIcon"], alt = "icon") {
        attrs {
            width = size.toString()
            height = size.toString()
        }
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

private fun Node.fitPlayerName(size: Int) {
    val maxFontHeight = (size * 0.31)
    val minFontHeight = (size * 0.16)
    fitty(maxFontHeight, minFontHeight, true)
}
