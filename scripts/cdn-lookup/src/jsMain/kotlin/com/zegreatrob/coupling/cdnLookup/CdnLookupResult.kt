package com.zegreatrob.coupling.cdnLookup

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class CdnLookupResult(val urls: Map<String, String>)

internal fun CdnLookupResult.toJson(): String = Json.encodeToString(this)

internal fun CdnLookupResult.toUrlMapJson(): String = Json.encodeToString(urls)
