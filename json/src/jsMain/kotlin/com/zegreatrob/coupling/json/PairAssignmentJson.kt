package com.zegreatrob.coupling.json

import kotlin.js.json

fun Array<Pair<String, Any?>>.pairsToJson() = json(*this)
