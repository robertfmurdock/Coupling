package com.zegreatrob.coupling.dynamo

import com.benasher44.uuid.uuid4
import com.soywiz.klock.*
import com.zegreatrob.coupling.dynamo.DynamoUserJsonMapping
import com.zegreatrob.coupling.dynamo.DynamoLiveInfoRepository
import com.zegreatrob.coupling.dynamo.RepositoryContext
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.validation.*
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.coupling.stubmodel.uuidString
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.js.json
import kotlin.test.Test

@Suppress("unused")
class DynamoLiveInfoRepositoryTest : LiveInfoRepositoryValidator<DynamoLiveInfoRepository> {

    override val repositorySetup = asyncTestTemplate<SharedContext<DynamoLiveInfoRepository>>(sharedSetup = {
        val clock = MagicClock()
        val userId = "${uuid4()}"
        val user = User(userId, "${uuid4()}", emptySet())
        val repository = DynamoLiveInfoRepository(userId, clock)
        SharedContextData(repository, clock, user)
    })

}
