package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.player.AvatarType

fun AvatarType.toSerializable() = name.let { GqlAvatarType.valueOfLabel(it) }
fun GqlAvatarType.toModel() = name.let { AvatarType.valueOf(it) }
