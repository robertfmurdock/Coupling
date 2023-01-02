@file:Suppress("unused")

package com.zegreatrob.coupling.client.external.domtoimage

import org.w3c.files.Blob
import web.html.HTMLElement
import kotlin.js.Promise

@JsModule("dom-to-image")
external val domToImage: DomToImage

external interface DomToImage {
    fun toPng(current: HTMLElement?): Promise<String>
    fun toBlob(current: HTMLElement?): Promise<Blob>
    fun toSvg(current: HTMLElement?): Promise<String>
}
