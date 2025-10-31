package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.sdk.schema.type.AvatarType

fun AvatarType.toModel() = when (this) {
    AvatarType.Retro -> com.zegreatrob.coupling.model.player.AvatarType.Retro
    AvatarType.RobohashSet1 -> com.zegreatrob.coupling.model.player.AvatarType.RobohashSet1
    AvatarType.RobohashSet2 -> com.zegreatrob.coupling.model.player.AvatarType.RobohashSet2
    AvatarType.RobohashSet3 -> com.zegreatrob.coupling.model.player.AvatarType.RobohashSet3
    AvatarType.RobohashSet4 -> com.zegreatrob.coupling.model.player.AvatarType.RobohashSet4
    AvatarType.RobohashSet5 -> com.zegreatrob.coupling.model.player.AvatarType.RobohashSet5
    AvatarType.Multiavatar -> com.zegreatrob.coupling.model.player.AvatarType.Multiavatar
    AvatarType.DicebearPixelArt -> com.zegreatrob.coupling.model.player.AvatarType.DicebearPixelArt
    AvatarType.DicebearAdventurer -> com.zegreatrob.coupling.model.player.AvatarType.DicebearAdventurer
    AvatarType.DicebearCroodles -> com.zegreatrob.coupling.model.player.AvatarType.DicebearCroodles
    AvatarType.DicebearThumbs -> com.zegreatrob.coupling.model.player.AvatarType.DicebearThumbs
    AvatarType.DicebearLorelei -> com.zegreatrob.coupling.model.player.AvatarType.DicebearLorelei
    AvatarType.UNKNOWN__ -> null
}
