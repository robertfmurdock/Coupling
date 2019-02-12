package com.zegreatrob.coupling.server.entity.user

import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.common.entity.user.User
import com.zegreatrob.coupling.server.DbRecordLoadSyntax
import com.zegreatrob.coupling.server.DbRecordSaveSyntax
import kotlin.js.Json
import kotlin.js.json

interface MongoUserRepository : UserRepository, DbRecordSaveSyntax, DbRecordLoadSyntax {

    val userCollection: dynamic

    override suspend fun save(user: User) {
        user.toDbJson()
                .save(userCollection)
    }

    override suspend fun getUser() = findByQuery(json("email" to userEmail), userCollection)
            .firstOrNull()
            ?.fromDbToUser()


    private fun User.toDbJson() = json(
            "email" to email,
            "tribes" to authorizedTribeIds.map { it.value }.toTypedArray()
    )

    private fun Json.fromDbToUser() = User(
            email = this["email"].toString(),
            authorizedTribeIds = this["tribes"]?.unsafeCast<Array<String>>()?.map { TribeId(it) } ?: emptyList()
    )

}

interface UserRepository {
    suspend fun save(user: User)
    suspend fun getUser(): User?
}
