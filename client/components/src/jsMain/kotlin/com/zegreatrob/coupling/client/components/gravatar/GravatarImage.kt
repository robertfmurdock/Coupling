package com.zegreatrob.coupling.client.components.gravatar

import com.zegreatrob.coupling.client.components.external.blueimpMd5.md5
import react.ChildrenBuilder
import react.dom.html.ReactHTML.img
import web.cssom.ClassName

fun gravatarUrl(email: String, options: GravatarOptions): String {
    val codedEmail = md5(email.lowercase().trim())
    return "https://www.gravatar.com/avatar/$codedEmail?default=${options.default}&s=${options.size}"
}

sealed external interface GravatarOptions {
    var default: String
    var size: Int
}

fun ChildrenBuilder.gravatarImage(
    email: String?,
    fallback: String? = null,
    className: ClassName? = null,
    alt: String?,
    options: GravatarOptions,
) = img {
    src = myGravatarUrl(options, email, fallback)
    this.alt = alt
    draggable = false
    width = options.size.toDouble()
    height = options.size.toDouble()
    className?.let { this.className = className }
}

fun myGravatarUrl(options: GravatarOptions, email: String?, fallback: String?) =
    if (email == null && fallback != null) {
        fallback
    } else {
        gravatarUrl(email ?: "", options)
    }
