package com.zegreatrob.coupling.repository.compound

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.repository.memory.MemoryPlayerRepository
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PartyContext
import com.zegreatrob.coupling.repository.validation.PlayerEmailRepositoryValidator
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.test.Test

class CompoundPlayerRepositoryTest : PlayerEmailRepositoryValidator<CompoundPlayerRepository> {

    override val repositorySetup = asyncTestTemplate<PartyContext<CompoundPlayerRepository>>(
        sharedSetup = {
            val clock = MagicClock()
            val stubUser = stubUser()

            val repository1 = MemoryPlayerRepository(stubUser.email, clock)
            val repository2 = MemoryPlayerRepository(stubUser.email, clock)

            val compoundRepo = CompoundPlayerRepository(repository1, repository2)

            object : PartyContext<CompoundPlayerRepository> {
                override val partyId = stubPartyId()
                override val repository = compoundRepo
                override val clock = clock
                override val user = stubUser
            }
        }
    )

    @Test
    fun saveWillWriteToSecondRepository() = asyncSetup(object {
        val stubUser = stubUser()

        val repository1 = MemoryPlayerRepository(stubUser.email, TimeProvider)
        val repository2 = MemoryPlayerRepository(stubUser.email, TimeProvider)

        val compoundRepo = CompoundPlayerRepository(repository1, repository2)

        val tribeId = stubPartyId()
        val player = stubPlayer()
    }) exercise {
        compoundRepo.save(tribeId.with(player))
    } verify {
        repository2.getPlayers(tribeId).map { it.data.player }.find { it.id == player.id }
            .assertIsEqualTo(player)
    }

    @Test
    fun deleteWillWriteToSecondRepository() = asyncSetup(object {
        val stubUser = stubUser()

        val repository1 = MemoryPlayerRepository(stubUser.email, TimeProvider)
        val repository2 = MemoryPlayerRepository(stubUser.email, TimeProvider)

        val compoundRepo = CompoundPlayerRepository(repository1, repository2)

        val tribeId = stubPartyId()
        val player = stubPlayer()
    }) exercise {
        compoundRepo.save(tribeId.with(player))
        compoundRepo.deletePlayer(tribeId, player.id)
    } verify {
        repository2.getPlayers(tribeId).map { it.data.player }.find { it.id == player.id }
            .assertIsEqualTo(null)
    }
}
