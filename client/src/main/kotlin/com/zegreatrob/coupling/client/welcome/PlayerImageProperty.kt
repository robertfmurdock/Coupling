package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.components.pngPath
import kotlin.reflect.KProperty

fun playerImage() = PlayerImageProperty()

class PlayerImageProperty {
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = pngPath("players/${property.name}")
}
