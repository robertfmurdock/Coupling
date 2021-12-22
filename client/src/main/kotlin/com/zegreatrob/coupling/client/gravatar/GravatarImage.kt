package com.zegreatrob.coupling.client.gravatar

import com.zegreatrob.coupling.client.external.blueimpMd5.md5
import react.create
import react.dom.html.ReactHTML.img

private fun gravatarUrl(email: String, options: GravatarOptions): String {
    val codedEmail = md5(email.lowercase().trim())
    return "https://www.gravatar.com/avatar/$codedEmail?default=${options.default}&s=${options.size}"
}

interface GravatarOptions {
    val default: String
    val size: Int
}

fun gravatarImage(
    email: String?,
    fallback: String? = null,
    className: String? = null,
    alt: String?,
    options: GravatarOptions
) = img.create {
    src = myGravatarUrl(options, email, fallback)
    this.alt = alt
    draggable = false
    width = options.size.toDouble()
    height = options.size.toDouble()
    className?.let { this.className = className }
}

private fun myGravatarUrl(options: GravatarOptions, email: String?, fallback: String?) =
    if (email == null && fallback != null) {
        fallback
    } else {
        gravatarUrl(email ?: "", options)
    }
