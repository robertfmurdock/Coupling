package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.model.tribe.TribeId
import org.w3c.dom.url.URLSearchParams
import react.RProps

data class PageProps(
    val pathParams: Map<String, String>,
    val pathSetter: (String) -> Unit,
    val search: URLSearchParams
) : RProps {
    val tribeId: TribeId? get() = pathParams["tribeId"]?.let(::TribeId)
    val playerId: String? get() = pathParams["playerId"]
}