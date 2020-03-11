package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PlayerEmailRepositoryValidator
import stubTribeId
import stubUser

@Suppress("unused")
class DynamoPlayerRepositoryTest : PlayerEmailRepositoryValidator<DynamoPlayerRepository> {

    override suspend fun withRepository(
        clock: MagicClock,
        handler: suspend (DynamoPlayerRepository, TribeId, User) -> Unit
    ) {
        val user = stubUser()
        handler(DynamoPlayerRepository(user.email, clock), stubTribeId(), user)
    }

}