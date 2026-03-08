@file:JsModule("d3-selection")

package com.zegreatrob.coupling.client.components.graphing.external.d3.selection

import com.zegreatrob.coupling.client.components.graphing.D3Thing
import web.html.HTMLElement

external fun select(element: HTMLElement): D3Thing
