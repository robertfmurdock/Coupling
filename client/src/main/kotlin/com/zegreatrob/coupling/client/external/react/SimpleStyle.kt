package com.zegreatrob.coupling.client.external.react

external interface SimpleStyle {
    val className: String
}

operator fun SimpleStyle.get(propertyName: String): String = let {
    @Suppress("UNUSED_VARIABLE") val prop = propertyName
    js("it[prop]").unsafeCast<String>()
}

fun <T> useStyles(path: String): T = loadStyles(path)
fun useStyles(path: String): SimpleStyle =
    loadStyles(path)