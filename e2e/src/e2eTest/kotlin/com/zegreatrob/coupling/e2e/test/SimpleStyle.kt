package com.zegreatrob.coupling.e2e.test

fun loadStyles(@Suppress("UNUSED_PARAMETER") name: String): SimpleStyle =
    js("require('../../../../../client/build/processedResources/js/main/com/zegreatrob/coupling/client/'+ name +'.css')")
        .unsafeCast<SimpleStyle>()

external interface SimpleStyle {
    val className: String
}

operator fun SimpleStyle.get(propertyName: String): String = let {
    @Suppress("UNUSED_VARIABLE")
    val prop = propertyName
    js("it[prop]").unsafeCast<String>()
}
