package com.zegreatrob.coupling.json

import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.json.encodeToDynamic
import kotlin.js.Json

inline fun <reified T> T.toJsonDynamic() = couplingJsonFormat.encodeToDynamic(this)
inline fun <reified T> Json.fromJsonDynamic() = couplingJsonFormat.decodeFromDynamic<T>(this)
