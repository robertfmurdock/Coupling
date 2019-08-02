package com.zegreatrob.coupling.client

import org.w3c.dom.url.URLSearchParams
import react.RProps

data class PageProps(
        val coupling: Coupling,
        val pathParams: Map<String, String>,
        val pathSetter: (String) -> Unit,
        val search: URLSearchParams
) : RProps