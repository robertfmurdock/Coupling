package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.party.SecretId
import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.SharedContext
import com.zegreatrob.coupling.repository.validation.SharedContextData
import com.zegreatrob.coupling.repository.validation.UserRepositoryValidator
import com.zegreatrob.coupling.stubmodel.stubUserDetails
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.TestTemplate
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotools.types.text.toNotBlankString
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Suppress("unused")
class MemoryUserRepositoryTest : UserRepositoryValidator<MemoryUserRepository> {

    override val repositorySetup: TestTemplate<SharedContext<MemoryUserRepository>>
        get() = asyncTestTemplate(sharedSetup = {
            val clock = MagicClock()
            val email = "${Uuid.random()}@mail.com".toNotBlankString().getOrThrow()
            val id = UserId.new()
            val user = UserDetails(id, email, emptySet(), emptySet(), null, null)
            val repository = MemoryUserRepository(id, clock)
            SharedContextData(repository, clock, user)
        })

    @Test
    fun sharedBackendConsumesSecretAcrossRepositoryInstances() = asyncSetup(object {
        val backend = MemoryUserBackend()
        val issuer = stubUserDetails().copy(connectedEmails = emptySet(), connectSecretId = null)
        val first = stubUserDetails().copy(connectedEmails = emptySet(), connectSecretId = null)
        val second = stubUserDetails().copy(connectedEmails = emptySet(), connectSecretId = null)
        val issuerRepository = MemoryUserRepository(issuer.id, Clock.System, backend)
        val firstRepository = MemoryUserRepository(first.id, Clock.System, backend)
        val secondRepository = MemoryUserRepository(second.id, Clock.System, backend)
        val secret = SecretId.new()
    }) {
        issuerRepository.save(issuer)
        firstRepository.save(first)
        secondRepository.save(second)
        issuerRepository.save(issuer.copy(connectSecretId = secret))
    } exercise {
        coroutineScope {
            val firstResult = async { firstRepository.connectUser(first, issuer.copy(connectSecretId = secret), secret) }
            val secondResult = async { secondRepository.connectUser(second, issuer.copy(connectSecretId = secret), secret) }
            firstResult.await() to secondResult.await()
        }
    } verify { (firstResult, secondResult) ->
        listOf(firstResult, secondResult).count { it }.assertIsEqualTo(1)
        val winner = if (firstResult) first else second
        issuerRepository.getUser()?.data.assertIsEqualTo(issuer.copy(connectedEmails = setOf(winner.email)))
        firstRepository.getUser()?.data.assertIsEqualTo(first.copy(connectedEmails = if (firstResult) setOf(issuer.email) else emptySet()))
        secondRepository.getUser()?.data.assertIsEqualTo(second.copy(connectedEmails = if (secondResult) setOf(issuer.email) else emptySet()))
    }
}
