package com.zegreatrob.coupling.repository.compound

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.memory.MemoryPlayerRepository
import com.zegreatrob.coupling.repository.validation.PlayerEmailRepositoryValidator
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import stubPlayer
import stubTribeId
import stubUser
import kotlin.test.Test

class CompoundPlayerRepositoryTest : PlayerEmailRepositoryValidator<CompoundPlayerRepository> {
    override suspend fun withRepository(handler: suspend (CompoundPlayerRepository, TribeId, User) -> Unit) {
        val stubUser = stubUser()

        val repository1 = MemoryPlayerRepository(stubUser.email, TimeProvider)
        val repository2 = MemoryPlayerRepository(stubUser.email, TimeProvider)

        val compoundRepo = CompoundPlayerRepository(repository1, repository2)
        handler(compoundRepo, stubTribeId(), stubUser)
    }

    @Test
    fun saveWillWriteToSecondRepository() = testAsync {
        setupAsync(object {
            val stubUser = stubUser()

            val repository1 = MemoryPlayerRepository(stubUser.email, TimeProvider)
            val repository2 = MemoryPlayerRepository(stubUser.email, TimeProvider)

            val compoundRepo = CompoundPlayerRepository(repository1, repository2)

            val tribeId = stubTribeId()
            val player = stubPlayer()
        }) exerciseAsync {
            compoundRepo.save(tribeId.with(player))
        } verifyAsync {
            repository2.getPlayers(tribeId).map { it.data.player }.find { it.id == player.id }
                .assertIsEqualTo(player)
        }
    }

    @Test
    fun deleteWillWriteToSecondRepository() = testAsync {
        setupAsync(object {
            val stubUser = stubUser()

            val repository1 = MemoryPlayerRepository(stubUser.email, TimeProvider)
            val repository2 = MemoryPlayerRepository(stubUser.email, TimeProvider)

            val compoundRepo = CompoundPlayerRepository(repository1, repository2)

            val tribeId = stubTribeId()
            val player = stubPlayer()
        }) exerciseAsync {
            compoundRepo.save(tribeId.with(player))
            compoundRepo.deletePlayer(tribeId, player.id!!)
        } verifyAsync {
            repository2.getPlayers(tribeId).map { it.data.player }.find { it.id == player.id }
                .assertIsEqualTo(null)
        }
    }
}