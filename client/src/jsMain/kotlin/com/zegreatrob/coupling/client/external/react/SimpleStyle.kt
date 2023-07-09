package com.zegreatrob.coupling.client.external.react

import web.cssom.ClassName

external interface SimpleStyle {
    val className: ClassName
}

operator fun SimpleStyle.get(propertyName: String): ClassName = let {
    @Suppress("UNUSED_VARIABLE")
    val prop = propertyName
    ClassName(js("it[prop]").unsafeCast<String>())
}
