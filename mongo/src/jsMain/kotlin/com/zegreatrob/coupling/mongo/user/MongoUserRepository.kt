package com.zegreatrob.coupling.mongo.user

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.mongo.DbRecordLoadSyntax
import com.zegreatrob.coupling.mongo.DbRecordSaveSyntax
import com.zegreatrob.coupling.mongo.player.JsonRecordSyntax
import com.zegreatrob.coupling.repository.user.UserRepository
import kotlin.js.Json
import kotlin.js.json

interface MongoUserRepository : UserRepository, DbRecordSaveSyntax, DbRecordLoadSyntax, JsonRecordSyntax {

    val userCollection: dynamic

    override suspend fun save(user: User) {
        user.toDbJson()
            .save(userCollection)
    }

    override suspend fun getUser() = findByQuery(json("email" to userEmail), userCollection, "email")
        .firstOrNull()
        ?.let { it.toDbRecord(it.fromDbToUser()) }

    private fun User.toDbJson() = json(
        "id" to id,
        "email" to email,
        "tribes" to authorizedTribeIds.map { it.value }.toTypedArray()
    )

    private fun Json.fromDbToUser() = User(
        id = this["id"]?.toString() ?: "${uuid4()}",
        email = this["email"].toString(),
        authorizedTribeIds = this["tribes"]?.unsafeCast<Array<String>>()?.map {
            TribeId(
                it
            )
        }?.toSet() ?: emptySet()
    )

}
