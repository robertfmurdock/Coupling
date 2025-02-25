package com.zegreatrob.coupling.client.components.stats

@JsModule("date-fns/formatDistance")
external val formatDistanceModule: dynamic

val formatDistance: (Int?, Int) -> String = if (formatDistanceModule["formatDistance"] != undefined) {
    formatDistanceModule["formatDistance"].unsafeCast<(Int?, Int) -> String>()
} else {
    formatDistanceModule.unsafeCast<(Int?, Int) -> String>()
}
