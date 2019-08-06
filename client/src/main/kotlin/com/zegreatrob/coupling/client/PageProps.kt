package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.common.entity.tribe.TribeId
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