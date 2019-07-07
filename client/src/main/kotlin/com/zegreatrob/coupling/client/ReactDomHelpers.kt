package com.zegreatrob.coupling.client

import kotlinx.html.IMG
import react.dom.RDOMBuilder

fun RDOMBuilder<IMG>.withAttributes(map: Map<String, Any?>) = map.ignoreNulls()
        .forEach {
            attrs[it.key] = it.value
        }

private fun Map<String, Any?>.ignoreNulls() = filterValues { it != null }
        .mapValues { it.value!! }
