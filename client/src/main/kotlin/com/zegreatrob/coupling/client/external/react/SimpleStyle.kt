package com.zegreatrob.coupling.client.external.react

import csstype.ClassName

external interface SimpleStyle {
    val className: ClassName
}

operator fun SimpleStyle.get(propertyName: String): ClassName = let {
    @Suppress("UNUSED_VARIABLE") val prop = propertyName
    ClassName(js("it[prop]").unsafeCast<String>())
}

fun useStyles(path: String): SimpleStyle = loadStyles(path)
