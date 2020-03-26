package com.zegreatrob.coupling.server

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.mongo.external.monk.MonkCollection
import kotlin.js.Json
import kotlin.js.json

fun userDataService(usersCollection: dynamic): Json {
    usersCollection.unsafeCast<MonkCollection>()
        .createIndex(json("email" to 1))

    return json(
        "findOrCreate" to { email: String, traceId: Uuid?, callback: (dynamic, dynamic) -> Unit ->
            authActionDispatcher(usersCollection, email, traceId)
                .then {
                    it.performFindOrCreateUserAction()
                }
                .then(onFulfilled = { user ->
                    callback(null, user)
                }, onRejected = { err ->
                    callback(err, null)
                })
        },
        "serializeUser" to { user: Json, done: (dynamic, dynamic) -> Unit ->
            if (user["email"] != null) {
                done(null, user["email"])
            } else {
                done("The user did not have an id to serialize", null)
            }
        },
        "deserializeUser" to { id: String, done: (dynamic, dynamic) -> Unit ->
            authActionDispatcher(usersCollection, id, null)
                .then { it.performFindOrCreateUserAction() }
                .then(onFulfilled = { user ->
                    done(null, user)
                }, onRejected = { done(it, null) })
        }
    )
}