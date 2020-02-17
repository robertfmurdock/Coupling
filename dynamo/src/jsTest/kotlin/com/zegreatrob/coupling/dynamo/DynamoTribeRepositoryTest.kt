package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.repository.validation.TribeRepositoryValidator

@Suppress("unused")
class DynamoTribeRepositoryTest :
    TribeRepositoryValidator {

    override suspend fun withRepository(handler: suspend (TribeRepository) -> Unit) {
        val repository = DynamoTribeRepository()
        handler(repository)
    }

}