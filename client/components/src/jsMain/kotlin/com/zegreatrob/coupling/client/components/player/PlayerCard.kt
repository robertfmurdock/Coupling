package com.zegreatrob.coupling.client.components.player

import com.zegreatrob.coupling.client.components.encodeURIComponent
import com.zegreatrob.coupling.client.components.gravatar.GravatarOptions
import com.zegreatrob.coupling.client.components.gravatar.gravatarUrl
import com.zegreatrob.coupling.client.components.gravatar.myGravatarUrl
import com.zegreatrob.coupling.client.components.pngPath
import com.zegreatrob.coupling.client.components.thirdPartyAvatarsDisabledContext
import com.zegreatrob.coupling.model.player.AvatarType
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import csstype.PropertiesBuilder
import emotion.react.css
import react.ChildrenBuilder
import react.Props
import react.create
import react.dom.events.MouseEvent
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import react.useCallback
import web.cssom.Angle
import web.cssom.Auto
import web.cssom.BackgroundRepeat
import web.cssom.Border
import web.cssom.BoxShadow
import web.cssom.ClassName
import web.cssom.Clear
import web.cssom.Color
import web.cssom.Display
import web.cssom.Flex
import web.cssom.LineStyle
import web.cssom.Margin
import web.cssom.NamedColor
import web.cssom.None
import web.cssom.Overflow
import web.cssom.Position
import web.cssom.TextAlign
import web.cssom.deg
import web.cssom.em
import web.cssom.number
import web.cssom.px
import web.cssom.rgb
import web.cssom.rotate
import web.cssom.s
import web.cssom.url
import kotlin.random.Random

external interface PlayerCardProps : Props {
    var player: Player
    var className: ClassName?
    var onClick: (() -> Unit)?
    var size: Int?
    var deselected: Boolean?
    var tilt: Angle?
}

@ReactFunc
val PlayerCard by nfc<PlayerCardProps> { props ->
    val (player, className, onClick) = props
    val onClickFunc: (MouseEvent<*, *>) -> Unit = useCallback(onClick) { onClick?.invoke() }
    val size = props.size ?: 100
    val tilt = props.tilt ?: 0.deg
    val deselected = props.deselected == true
    div {
        css(className) {
            playerCardStyles(tilt, deselected)
            "img" { display = Display.block }
            playerCardRuleSet(size)
        }
        asDynamic()["data-player-id"] = player.id.value.toString()
        asDynamic()["data-selected"] = "${!deselected}"
        this.onClick = onClickFunc
        div {
            css {
                margin = ((size * 0.02).px)
                perspective = 15.em
            }
            playerGravatarImage(player, size)
            PlayerCardHeader(player, size)
        }
    }
}

private fun PropertiesBuilder.playerCardStyles(tilt: Angle, deselected: Boolean) {
    position = Position.relative
    clear = Clear.both
    display = Display.inlineBlock
    overflow = Overflow.visible
    border = Border(3.px, LineStyle.outset, Color("#dab8018f"))
    backgroundImage = url(pngPath("overlay"))
    backgroundRepeat = BackgroundRepeat.repeatX
    textAlign = TextAlign.center
    textDecoration = None.none
    color = NamedColor.black
    margin = Margin(0.px, 2.px, 0.px, 2.px)
    transitionDuration = 0.25.s
    transform = rotate(tilt)

    if (deselected) {
        top = 30.px
        backgroundColor = Color("#ca6363")
    } else {
        top = 0.px
        backgroundColor = NamedColor.darkseagreen
    }
}

private fun PropertiesBuilder.playerCardRuleSet(size: Int) {
    val sizeInEm = size.inCouplingEm()
    val totalExtraMarginNeededForImage = 2 * (sizeInEm * 0.02)
    width = (sizeInEm + totalExtraMarginNeededForImage).em
    height = (sizeInEm * 1.4).em
    borderWidth = (sizeInEm * 0.04).em
    borderRadius = (sizeInEm * 0.08).em
    boxShadow = BoxShadow((sizeInEm * 0.02).em, (sizeInEm * 0.04).em, (sizeInEm * 0.04).em, rgb(0, 0, 0, 0.6))
    flex = Flex(number(0.0), number(0.0), Auto.auto)
}

fun Int.inCouplingEm(): Double = this / 14.0

val noPlayerImagePath = pngPath("players/autumn")

private fun ChildrenBuilder.playerGravatarImage(player: Player, size: Int) = thirdPartyAvatarsDisabledContext.Consumer {
    children = { disabled ->
        img.create {
            alt = "player-icon"
            css {
                width = size.inCouplingEm().em
                height = size.inCouplingEm().em
            }
            src = when {
                disabled -> noPlayerImagePath
                player.imageURL != null -> player.imageURL
                player.avatarType != null -> player.getDirectAvatarImageUrl(size, player.avatarType!!)
                else -> player.getGravatarSafeAvatarImageUrl(size, player.avatarType ?: player.hashedRandomAvatar())
            }
        }
    }
}

private fun String.gravatarWrapper(email: String, size: Int) = gravatarUrl(
    email,
    GravatarOptions(
        size = size,
        default = encodeURIComponent(this@gravatarWrapper),
    ),
)

private fun Player.hashedRandomAvatar() = emailWithFallback()
    .hashCode()
    .let(::Random)
    .nextInt(AvatarType.entries.size)
    .let { AvatarType.entries[it] }

private fun Player.getGravatarSafeAvatarImageUrl(size: Int, avatarType: AvatarType) = when (avatarType) {
    AvatarType.RobohashSet1 -> getRobohashImageUrl("set1", "&gravatar=yes")
    AvatarType.RobohashSet2 -> getRobohashImageUrl("set2", "&gravatar=yes")
    AvatarType.RobohashSet3 -> getRobohashImageUrl("set3", "&gravatar=yes")
    AvatarType.RobohashSet4 -> getRobohashImageUrl("set4", "&gravatar=yes")
    AvatarType.RobohashSet5 -> getRobohashImageUrl("set5", "&gravatar=yes")
    AvatarType.Multiavatar -> multiavatarUrl("png").gravatarWrapper(emailWithFallback(), size)
    AvatarType.DicebearPixelArt -> gravatarDicebearUrl("pixel-art", size)
    AvatarType.DicebearAdventurer -> gravatarDicebearUrl("adventurer", size)
    AvatarType.DicebearCroodles -> gravatarDicebearUrl("croodles", size)
    AvatarType.DicebearThumbs -> gravatarDicebearUrl("thumbs", size)
    AvatarType.DicebearLorelei -> gravatarDicebearUrl("lorelei", size)
    else -> myGravatarUrl(
        options = GravatarOptions(size = size, default = "retro"),
        email = emailWithFallback(),
        fallback = null,
    )
}

private fun Player.getDirectAvatarImageUrl(size: Int, avatarType: AvatarType) = when (avatarType) {
    AvatarType.Retro -> myGravatarUrl(
        GravatarOptions(
            size = size,
            default = "retro",
        ),
        emailWithFallback(),
        null,
    )

    AvatarType.RobohashSet1 -> getRobohashImageUrl("set1")
    AvatarType.RobohashSet2 -> getRobohashImageUrl("set2")
    AvatarType.RobohashSet3 -> getRobohashImageUrl("set3")
    AvatarType.RobohashSet4 -> getRobohashImageUrl("set4")
    AvatarType.RobohashSet5 -> getRobohashImageUrl("set5")
    AvatarType.Multiavatar -> multiavatarUrl("svg")
    AvatarType.DicebearPixelArt -> dicebearUrl("pixel-art", size, "svg")
    AvatarType.DicebearAdventurer -> dicebearUrl("adventurer", size, "svg")
    AvatarType.DicebearCroodles -> dicebearUrl("croodles", size, "svg")
    AvatarType.DicebearThumbs -> dicebearUrl("thumbs", size, "svg")
    AvatarType.DicebearLorelei -> dicebearUrl("lorelei", size, "svg")
}

private fun Player.gravatarDicebearUrl(set: String, size: Int) = dicebearUrl(set, size, "png")
    .gravatarWrapper(emailWithFallback(), size)

private fun Player.dicebearUrl(set: String, size: Int, type: String) = "https://api.dicebear.com/6.x/$set/$type/seed=${emailWithFallback()}&size=$size"

private fun Player.multiavatarUrl(type: String) = "https://api.multiavatar.com/${emailWithFallback()}.$type"

private fun Player.getRobohashImageUrl(setName: String, additionalArgs: String = "") = "https://robohash.org/${emailWithFallback()}?set=$setName$additionalArgs"

private fun Player.emailWithFallback() = when {
    email != "" -> email
    name != "" -> name
    else -> "name"
}
