package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.repositoryvalidation.TribeRepositoryValidator

@Suppress("unused")
class DynamoTribeRepositoryTest : TribeRepositoryValidator {

    override suspend fun withRepository(handler: suspend (TribeRepository) -> Unit) {
        val repository = dynamoTribeRepository()
        handler(repository)
    }

}