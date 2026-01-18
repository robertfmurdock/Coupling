package com.zegreatrob.coupling.cli.external.open

import kotlin.js.Promise

@JsModule("open")
external fun open(url: String): Promise<Unit>
