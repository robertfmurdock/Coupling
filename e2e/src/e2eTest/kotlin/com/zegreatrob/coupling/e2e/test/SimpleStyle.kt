package com.zegreatrob.coupling.e2e.test

external interface SimpleStyle {
    val className: String
}

operator fun SimpleStyle.get(propertyName: String): String = let {
    @Suppress("UNUSED_VARIABLE")
    val prop = propertyName
    js("it[prop]").unsafeCast<String>()
}
