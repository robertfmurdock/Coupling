package com.zegreatrob.coupling.client.external.react

private fun Map<String, Any?>.ignoreNulls() = filterValues { it != null }
    .mapValues { it.value!! }
