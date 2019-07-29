package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.blueimpMd5.md5
import kotlinx.html.classes
import react.RBuilder
import react.dom.img

private fun gravatarUrl(email: String, options: GravatarOptions): String {
    val codedEmail = md5(email.toLowerCase().trim())
    return "https://www.gravatar.com/avatar/$codedEmail?default=${options.default}&s=${options.size}"
}

interface GravatarOptions {
    val default: String
    val size: Int
}

fun RBuilder.gravatarImage(
        email: String?,
        fallback: String? = null,
        className: String? = null,
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
            gravatarUrl(email ?: "", options)
        }
