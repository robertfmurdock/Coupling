package com.zegreatrob.coupling.client

import kotlin.reflect.KProperty

fun playerImage() = PlayerImageProperty()

class PlayerImageProperty {
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = imagePath("players/${property.name}")
}