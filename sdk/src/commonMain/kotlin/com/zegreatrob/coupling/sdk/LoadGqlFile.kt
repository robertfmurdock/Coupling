package com.zegreatrob.coupling.sdk

import kotlin.reflect.KProperty

object LoadGqlFile {
    operator fun getValue(holder: Any, property: KProperty<*>): String = loadTextFile(property.name)
}