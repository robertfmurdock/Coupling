package com.zegreatrob.coupling.client

import kotlinx.html.classes
import react.RBuilder
import react.dom.img

private val gravatarUrl: dynamic = js("require('../../../app/components/player-card/GravatarHelper').gravatarUrl")

external interface GravatarOptions {
    @Suppress("unused")
    val default: String
    val size: Int
}

fun RBuilder.gravatarImage(
        email: String?,
        fallback: String? = null,
        className: String?,
        alt: String?,
        options: GravatarOptions
) = img(
        src = myGravatarUrl(options, email, fallback),
        alt = alt
) {
    attrs {
        width = options.size.toString()
        height = options.size.toString()
        className?.let {
            classes += className
        }
    }
}

private fun myGravatarUrl(options: GravatarOptions, email: String?, fallback: String?) =
        if (email == null && fallback != null) {
            fallback
        } else {
            gravatarUrl(email, options) as String
        }
