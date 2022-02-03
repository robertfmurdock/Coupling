package com.zegreatrob.coupling.sdk

import org.w3c.dom.Window
import org.w3c.dom.get

actual fun getLocationAndBasename(): Pair<String, String>? = js("global.window")
    .unsafeCast<Window?>()
    ?.let { window ->
        val location = window.location.origin
        val basename = "${window["basename"]}"
        return location to basename
    }