package com.zegreatrob.coupling.client

@Suppress("NOTHING_TO_INLINE")
inline fun imagePath(@Suppress("UNUSED_PARAMETER") imageName: String) = js("require('' + imageName +'.png')")
    .default.unsafeCast<String>()
    .let { "/app/build/$it" }