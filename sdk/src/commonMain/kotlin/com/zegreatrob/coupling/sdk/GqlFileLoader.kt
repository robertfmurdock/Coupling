package com.zegreatrob.coupling.sdk

import kotlin.reflect.KProperty

object GqlFileLoader {
    operator fun getValue(holder: Any, property: KProperty<*>): String = loadGqlFile(property.name)
}
