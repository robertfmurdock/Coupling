package com.zegreatrob.coupling.client.external.domtoimage

import org.w3c.dom.Node
import kotlin.js.Promise

@JsModule("dom-to-image")
external val domToImage: DomToImage

external interface DomToImage {
    fun toPng(current: Node?): Promise<String>
    fun toSvg(current: Node?): Promise<String>
}
