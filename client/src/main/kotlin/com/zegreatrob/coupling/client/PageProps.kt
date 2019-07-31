package com.zegreatrob.coupling.client

import react.RProps

data class PageProps(
        val coupling: Coupling,
        val pathParams: Map<String, List<String>>,
        val pathSetter: (String) -> Unit
) : RProps