package com.zegreatrob.coupling.repository.compound

import com.zegreatrob.coupling.model.party.SecretId
import com.zegreatrob.coupling.repository.memory.MemoryUserBackend
import com.zegreatrob.coupling.repository.memory.MemoryUserRepository
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.SharedContext
import com.zegreatrob.coupling.repository.validation.SharedContextData
import com.zegreatrob.coupling.repository.validation.UserRepositoryValidator
import com.zegreatrob.coupling.stubmodel.stubUserDetails
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.test.Test
import kotlin.time.Clock

class CompoundUserRepositoryTest : UserRepositoryValidator<CompoundUserRepository> {

    override val repositorySetup = asyncTestTemplate<SharedContext<CompoundUserRepository>>(sharedSetup = {
        val clock = MagicClock()
        val stubUser = stubUserDetails()

        val repository1 = MemoryUserRepository(stubUser.id, clock)
        val repository2 = MemoryUserRepository(stubUser.id, clock)

        val compoundRepo = CompoundUserRepository(repository1, repository2)
        SharedContextData(compoundRepo, clock, stubUser)
    })

    @Test
    fun saveWillSaveToSecondRepositoryAsWell() = asyncSetup(object {
        val user = stubUserDetails()

        val repository1 = MemoryUserRepository(user.id, Clock.System)
        val repository2 = MemoryUserRepository(user.id, Clock.System)

        val compoundRepo = CompoundUserRepository(repository1, repository2)
    }) exercise {
        compoundRepo.save(user)
    } verify {
        repository2.getUser()?.data
            .assertIsEqualTo(user)
    }

    @Test
    fun connectionUsesThePrimaryRepositoryAsItsSingleAuthority() = asyncSetup(object {
        val backend = MemoryUserBackend()
        val issuer = stubUserDetails().copy(connectedEmails = emptySet(), connectSecretId = null)
        val caller = stubUserDetails().copy(connectedEmails = emptySet(), connectSecretId = null)
        val issuerRepository = MemoryUserRepository(issuer.id, Clock.System, backend)
        val primary = MemoryUserRepository(caller.id, Clock.System, backend)
        val secondary = MemoryUserRepository(caller.id, Clock.System)
        val repository = CompoundUserRepository(primary, secondary)
        val secret = SecretId.new()
    }) {
        issuerRepository.save(issuer)
        repository.save(caller)
        issuerRepository.save(issuer.copy(connectSecretId = secret))
    } exercise {
        repository.connectUser(caller, issuer.copy(connectSecretId = secret), secret) to repository.connectUser(caller, issuer.copy(connectSecretId = secret), secret)
    } verify { result ->
        result.assertIsEqualTo(true to false)
        repository.getUser()?.data.assertIsEqualTo(caller.copy(connectedEmails = setOf(issuer.email)))
        issuerRepository.getUser()?.data.assertIsEqualTo(issuer.copy(connectedEmails = setOf(caller.email)))
    }
}
