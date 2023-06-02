package com.zegreatrob.coupling.model.player

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.party.PartyElement

data class Player(
    val id: String = "${uuid4()}",
    val badge: Int = Badge.Default.value,
    val name: String = "",
    val email: String = "",
    val callSignAdjective: String = "",
    val callSignNoun: String = "",
    val imageURL: String? = null,
    val avatarType: AvatarType?,
)

val defaultPlayer = Player(id = "DEFAULT", avatarType = null)

val PartyElement<Player>.player get() = element

enum class AvatarType(val attribution: String?) {
    Retro(null),
    RobohashSet1(attribution = "https://robohash.org/"),
    RobohashSet2(attribution = "https://robohash.org/"),
    RobohashSet3(attribution = "https://robohash.org/"),
    RobohashSet4(attribution = "https://robohash.org/"),
    RobohashSet5(attribution = "https://robohash.org/"),
    BoringBeam(attribution = "https://boringavatars.com/"),
    BoringBauhaus(attribution = "https://boringavatars.com/"),
    Multiavatar(attribution = "https://multiavatar.com/"),
    DicebearPixelArt(attribution = "https://www.dicebear.com/styles/pixel-art"),
    DicebearAdventurer(attribution = "https://www.dicebear.com/styles/adventurer"),
    DicebearCroodles(attribution = "https://www.dicebear.com/styles/croodles"),
    DicebearThumbs(attribution = "https://www.dicebear.com/styles/thumbs"),
    DicebearLorelei(attribution = "https://www.dicebear.com/styles/lorelei"),
}
