package com.zegreatrob.coupling.coremongo

import kotlin.js.json

val monk = js("require(\"monk\")")

interface MonkToolkit {

    fun id(): String {
        return monk.id().toString()
    }

    fun jsRepository(mongoUrl: String): dynamic {
        val db = monk.default(mongoUrl)
        return json(
            "playersCollection" to db.get("players"),
            "historyCollection" to db.get("history"),
            "tribesCollection" to db.get("tribes"),
            "pinCollection" to db.get("pins")
        )
    }

    fun getCollection(collectionName: String, mongoUrl: String): dynamic {
        val db = monk.default(mongoUrl)
        return db.get(collectionName)
    }
}
