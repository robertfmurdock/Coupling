package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.gravatar.gravatarUrl
import com.zegreatrob.coupling.client.components.gravatar.myGravatarUrl
import com.zegreatrob.coupling.model.player.AvatarType
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.ntmFC
import csstype.PropertiesBuilder
import emotion.react.css
import js.core.jso
import react.ChildrenBuilder
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
import web.cssom.rotate
import web.cssom.s
import web.cssom.url
import kotlin.random.Random

data class PlayerCard(
    val player: Player,
    val className: ClassName? = null,
    val size: Int = 100,
    val onClick: () -> Unit = {},
    val deselected: Boolean = false,
    val tilt: Angle = 0.deg,
) : DataPropsBind<PlayerCard>(playerCard)

val playerCard by ntmFC<PlayerCard> { (player, className, size, onClick, deselected, tilt) ->
    val onClickFunc: (MouseEvent<*, *>) -> Unit = useCallback(onClick) { onClick() }
    div {
        css(className) {
            playerCardStyles(tilt, deselected)
            "img" { display = Display.block }
            playerCardRuleSet(size)
        }
        asDynamic()["data-player-id"] = player.id
        asDynamic()["data-selected"] = "${!deselected}"
        this.onClick = onClickFunc
        div {
            css {
                margin = ((size * 0.02).px)
                perspective = 15.em
            }
            playerGravatarImage(player, size)
            add(PlayerCardHeader(player, size))
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
    val totalExtraMarginNeededForImage = 2 * (size * 0.02)
    width = (size + totalExtraMarginNeededForImage).px
    height = (size * 1.4).px
    borderWidth = (size * 0.04).px
    borderRadius = (size * 0.08).px
    boxShadow = BoxShadow((size * 0.02).px, (size * 0.04).px, (size * 0.04).px, Color("rgba(0, 0, 0, 0.6)"))
    flex = Flex(number(0.0), number(0.0), Auto.auto)
}

private fun ChildrenBuilder.playerGravatarImage(player: Player, size: Int) = img {
    alt = "player-icon"
    width = size.toDouble()
    height = size.toDouble()
    src = when {
        player.imageURL != null -> player.imageURL
        player.avatarType != null -> player.getDirectAvatarImageUrl(size, player.avatarType!!)
        else -> player.getGravatarSafeAvatarImageUrl(size, player.avatarType ?: player.hashedRandomAvatar())
    }
}

private fun String.gravatarWrapper(email: String, size: Int) = gravatarUrl(
    email,
    jso {
        this.size = size
        this.default = encodeURIComponent(this@gravatarWrapper)
    },
)

private fun Player.hashedRandomAvatar() = emailWithFallback()
    .hashCode()
    .let(::Random)
    .nextInt(AvatarType.values().size)
    .let { AvatarType.values()[it] }

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
    else -> myGravatarUrl(jso { this.size = size; this.default = "retro" }, emailWithFallback(), null)
}

private fun Player.getDirectAvatarImageUrl(size: Int, avatarType: AvatarType) = when (avatarType) {
    AvatarType.Retro -> myGravatarUrl(jso { this.size = size; this.default = "retro" }, emailWithFallback(), null)
    AvatarType.RobohashSet1 -> getRobohashImageUrl("set1")
    AvatarType.RobohashSet2 -> getRobohashImageUrl("set2")
    AvatarType.RobohashSet3 -> getRobohashImageUrl("set3")
    AvatarType.RobohashSet4 -> getRobohashImageUrl("set4")
    AvatarType.RobohashSet5 -> getRobohashImageUrl("set5")
    AvatarType.BoringBeam -> boringUrl(size, "beam")
    AvatarType.BoringBauhaus -> boringUrl(size, "bauhaus")
    AvatarType.Multiavatar -> multiavatarUrl("svg")
    AvatarType.DicebearPixelArt -> dicebearUrl("pixel-art", size, "svg")
    AvatarType.DicebearAdventurer -> dicebearUrl("adventurer", size, "svg")
    AvatarType.DicebearCroodles -> dicebearUrl("croodles", size, "svg")
    AvatarType.DicebearThumbs -> dicebearUrl("thumbs", size, "svg")
    AvatarType.DicebearLorelei -> dicebearUrl("lorelei", size, "svg")
}

private fun Player.gravatarDicebearUrl(set: String, size: Int) = dicebearUrl(set, size, "png")
    .gravatarWrapper(emailWithFallback(), size)

private fun Player.dicebearUrl(set: String, size: Int, type: String) =
    "https://api.dicebear.com/6.x/$set/$type/seed=${emailWithFallback()}&size=$size"

private fun Player.multiavatarUrl(type: String) = "https://api.multiavatar.com/${emailWithFallback()}.$type"

private fun Player.boringUrl(size: Int, boringSet: String) =
    "https://source.boringavatars.com/$boringSet/$size/${emailWithFallback()}?colors=E22092,170409,FF8C00,FAF0D2,9FB7C6"

private fun Player.getRobohashImageUrl(setName: String, additionalArgs: String = "") =
    "https://robohash.org/${emailWithFallback()}?set=$setName$additionalArgs"

private fun Player.emailWithFallback() = when {
    email != "" -> email
    name != "" -> name
    else -> "name"
}
