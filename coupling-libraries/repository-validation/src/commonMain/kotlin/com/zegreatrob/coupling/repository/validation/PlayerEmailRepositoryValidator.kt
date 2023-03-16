package com.zegreatrob.coupling.repository.validation

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.player.PlayerListGetByEmail
import com.zegreatrob.coupling.repository.player.PlayerRepository
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test

interface PlayerEmailRepositoryValidator<R> : PlayerRepositoryValidator<R>
    where R : PlayerRepository, R : PlayerListGetByEmail {

    @Test
    fun getPlayersForEmailsWillReturnLatestVersionOfPlayers() = repositorySetup.with(
        object : PartyContextMint<R>() {
            val email = "test-${uuid4()}@zegreatrob.com"
            val player = stubPlayer().copy(email = email)
            val redHerring = stubPlayer().copy(email = "something else")
            val updatedPlayer = player.copy(name = "Besto")
        }.bind(),
    ) exercise {
        with(repository) {
            save(partyId.with(player))
            save(partyId.with(redHerring))
            save(partyId.with(updatedPlayer))
        }
    } verifyWithWait {
        repository.getPlayerIdsByEmail(email)
            .assertIsEqualTo(listOf(partyId.with(player.id)), "Could not find by email <$email>")
    }

    @Test
    fun getPlayersForEmailsWillNotIncludePlayersThatChangedTheirEmailToSomethingElse() =
        repositorySetup.with(
            object : PartyContextMint<R>() {
                val email = "test-${uuid4()}@zegreatrob.com"
                val player = stubPlayer().copy(email = email)
                val updatedPlayer = player.copy(name = "Besto", email = "something else ")
            }.bind(),
        ) exercise {
            repository.save(partyId.with(player))
            repository.save(partyId.with(updatedPlayer))
        } verifyWithWait {
            repository.getPlayerIdsByEmail(email)
                .assertIsEqualTo(emptyList())
        }

    @Test
    fun getPlayersForEmailsWillNotIncludePlayersThatHaveBeenRemoved() =
        repositorySetup.with(
            object : PartyContextMint<R>() {
                val email = "test-${uuid4()}@zegreatrob.com"
                val player = stubPlayer().copy(email = email)
            }.bind(),
        ) exercise {
            repository.save(partyId.with(player))
            repository.deletePlayer(partyId, player.id)
        } verifyWithWait {
            repository.getPlayerIdsByEmail(email)
                .assertIsEqualTo(emptyList())
        }
}
