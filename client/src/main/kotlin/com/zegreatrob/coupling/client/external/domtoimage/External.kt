@file:Suppress("unused")

package com.zegreatrob.coupling.client.external.domtoimage

import org.w3c.dom.Node
import org.w3c.files.Blob
import kotlin.js.Promise

@JsModule("dom-to-image")
external val domToImage: DomToImage

external interface DomToImage {
    fun toPng(current: Node?): Promise<String>
    fun toBlob(current: Node?): Promise<Blob>
    fun toSvg(current: Node?): Promise<String>
}
