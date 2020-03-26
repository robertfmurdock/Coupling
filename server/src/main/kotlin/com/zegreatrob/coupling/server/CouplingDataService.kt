package com.zegreatrob.coupling.server

import kotlin.js.Json
import kotlin.js.json

fun couplingDataService(mongoUrl: String): Json {
    val database = com.zegreatrob.coupling.mongo.external.monk.default(mongoUrl)
    return json(
        "database" to database,
        "playersCollection" to database.get("players"),
        "historyCollection" to database.get("history"),
        "tribesCollection" to database.get("tribes"),
        "pinCollection" to database.get("pins"),
        "usersCollection" to database.get("users")
    )
}