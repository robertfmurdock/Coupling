package com.zegreatrob.coupling.client.external.react

import kotlinx.html.IMG
import react.dom.RDOMBuilder

private fun Map<String, Any?>.ignoreNulls() = filterValues { it != null }
    .mapValues { it.value!! }
