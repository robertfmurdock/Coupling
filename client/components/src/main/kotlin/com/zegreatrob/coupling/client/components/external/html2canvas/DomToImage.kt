package com.zegreatrob.coupling.client.components.external.html2canvas

import web.html.HTMLCanvasElement
import web.html.HTMLElement
import kotlin.js.Json
import kotlin.js.Promise

@JsModule("html2canvas")
external fun html2canvas(element: HTMLElement, options: Json = definedExternally): Promise<HTMLCanvasElement>
