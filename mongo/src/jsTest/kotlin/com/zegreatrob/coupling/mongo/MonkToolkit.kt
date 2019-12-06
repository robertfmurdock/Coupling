package com.zegreatrob.coupling.mongo

import kotlin.js.json

val monk = js("require(\"monk\")")

interface MonkToolkit {

    fun id() = monk.id().toString()

    fun getDb(mongoUrl: String) = monk.default(mongoUrl)

    fun jsRepository(db: dynamic) = json(
        "playersCollection" to db.get("players"),
        "historyCollection" to db.get("history"),
        "tribesCollection" to db.get("tribes"),
        "pinCollection" to db.get("pins")
    )

    fun getCollection(collectionName: String, db: dynamic) = db.get(collectionName)
}
