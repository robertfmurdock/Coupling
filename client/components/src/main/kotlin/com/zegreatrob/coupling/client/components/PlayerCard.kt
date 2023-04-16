package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.gravatar.gravatarUrl
import com.zegreatrob.coupling.client.components.gravatar.myGravatarUrl
import com.zegreatrob.coupling.model.player.AvatarType
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.Angle
import csstype.Auto
import csstype.BackgroundRepeat
import csstype.Border
import csstype.BoxShadow
import csstype.ClassName
import csstype.Clear
import csstype.Color
import csstype.Display
import csstype.Flex
import csstype.LineStyle
import csstype.Margin
import csstype.NamedColor
import csstype.None
import csstype.Overflow
import csstype.Position
import csstype.PropertiesBuilder
import csstype.TextAlign
import csstype.deg
import csstype.number
import csstype.px
import csstype.rotate
import csstype.s
import csstype.url
import emotion.react.css
import js.core.jso
import react.ChildrenBuilder
import react.dom.events.MouseEvent
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import react.useCallback
import kotlin.random.Random

data class PlayerCard(
    val player: Player,
    val className: ClassName? = null,
    val size: Int = 100,
    val onClick: () -> Unit = {},
    val deselected: Boolean = false,
    val tilt: Angle = 0.deg,
) : DataPropsBind<PlayerCard>(playerCard)

val playerCard = tmFC<PlayerCard> { (player, className, size, onClick, deselected, tilt) ->
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
            css { margin = ((size * 0.02).px) }
            playerGravatarImage(player, size)
            add(PlayerCardHeader(player, size))
        }
    }
}

private fun PropertiesBuilder.playerCardStyles(tilt: Angle, deselected: Boolean) {
    position = Position.relative
    clear = Clear.both
    display = Display.inlineBlock
    overflow = Overflow.hidden
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

private fun ChildrenBuilder.playerGravatarImage(player: Player, size: Int) = when {
    player.imageURL != null -> img {
        this.src = player.imageURL
        alt = "player-icon"
        this.width = size.toDouble()
        this.height = size.toDouble()
    }

    else -> img {
        src = player.getAvatarImageUrl(size, player.avatarType ?: player.hashedRandomAvatar())
        alt = "player-icon"
        draggable = false
        width = size.toDouble()
        height = size.toDouble()
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

private fun Player.getAvatarImageUrl(size: Int, avatarType: AvatarType) = when (avatarType) {
    AvatarType.Retro -> myGravatarUrl(jso { this.size = size; this.default = "retro" }, emailWithFallback(), null)
        .gravatarWrapper(emailWithFallback(), size)
    AvatarType.RobohashSet1 -> getRobohashImageUrl("set1")
    AvatarType.RobohashSet2 -> getRobohashImageUrl("set2")
    AvatarType.RobohashSet3 -> getRobohashImageUrl("set3")
    AvatarType.RobohashSet4 -> getRobohashImageUrl("set4")
    AvatarType.RobohashSet5 -> getRobohashImageUrl("set5")
    AvatarType.BoringBeam -> boringUrl(size, "beam")
    AvatarType.BoringBauhaus -> boringUrl(size, "bauhaus")
    AvatarType.Multiavatar -> multiavatarUrl()
        .gravatarWrapper(emailWithFallback(), size)
    AvatarType.DicebearPixelArt -> gravatarDicebearUrl("pixel-art", size)
    AvatarType.DicebearAdventurer -> gravatarDicebearUrl("adventurer", size)
    AvatarType.DicebearCroodles -> gravatarDicebearUrl("croodles", size)
    AvatarType.DicebearThumbs -> gravatarDicebearUrl("thumbs", size)
    AvatarType.DicebearLorelei -> gravatarDicebearUrl("lorelei", size)
}

private fun Player.gravatarDicebearUrl(set: String, size: Int) =
    "https://api.dicebear.com/6.x/$set/png/seed=${emailWithFallback()}&size=$size"
        .gravatarWrapper(emailWithFallback(), size)

private fun Player.multiavatarUrl() = "https://api.multiavatar.com/${emailWithFallback()}.png"

private fun Player.boringUrl(size: Int, boringSet: String) =
    "https://source.boringavatars.com/$boringSet/$size/${emailWithFallback()}?colors=E22092,170409,FF8C00,FAF0D2,9FB7C6"

private fun Player.getRobohashImageUrl(setName: String) =
    "https://robohash.org/${emailWithFallback()}?gravatar=yes&set=" +
        setName

private fun Player.emailWithFallback() = when {
    email != "" -> email
    name != "" -> name
    else -> "name"
}
