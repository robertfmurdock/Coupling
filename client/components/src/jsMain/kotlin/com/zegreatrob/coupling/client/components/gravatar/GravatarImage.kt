package com.zegreatrob.coupling.client.components.gravatar

import kotlinx.js.JsPlainObject
import org.kotlincrypto.hash.md.MD5
import react.ChildrenBuilder
import react.dom.html.ReactHTML
import web.cssom.ClassName

private val md5 = MD5()

fun gravatarUrl(email: String, options: GravatarOptions): String {
    val codedEmail = md5(email.lowercase().trim())
    return "https://www.gravatar.com/avatar/$codedEmail?default=${options.default}&s=${options.size}"
}

private fun md5(value: String): String = md5.digest(value.encodeToByteArray())
    .toHexString()

@JsPlainObject
sealed external interface GravatarOptions {
    val default: String
    val size: Int
}

fun ChildrenBuilder.gravatarImage(
    email: String?,
    fallback: String? = null,
    className: ClassName? = null,
    alt: String?,
    options: GravatarOptions,
) = ReactHTML.img {
    src = myGravatarUrl(options, email, fallback)
    this.alt = alt
    draggable = false
    width = options.size.toDouble()
    height = options.size.toDouble()
    className?.let { this.className = className }
}

fun myGravatarUrl(options: GravatarOptions, email: String?, fallback: String?) = if (email == null && fallback != null) {
    fallback
} else {
    gravatarUrl(email ?: "", options)
}
