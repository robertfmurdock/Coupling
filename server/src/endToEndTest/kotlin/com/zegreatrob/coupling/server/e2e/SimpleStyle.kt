package com.zegreatrob.coupling.server.e2e

fun loadStyles(@Suppress("UNUSED_PARAMETER") name: String): SimpleStyle {
    return js("require('../../../../../client/build/processedResources/Js/main/com/zegreatrob/coupling/client/'+ name +'.css')")
        .locals
        .unsafeCast<SimpleStyle>()
}

external interface SimpleStyle {

    val className: String
}

operator fun SimpleStyle.get(propertyName: String): String = let {
    @Suppress("UNUSED_VARIABLE") val prop = propertyName
    js("it[prop]").unsafeCast<String>()
}
