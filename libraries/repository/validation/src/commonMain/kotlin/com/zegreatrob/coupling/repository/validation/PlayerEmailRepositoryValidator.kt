package com.zegreatrob.coupling.repository.validation

import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.player.PlayerListGetByEmail
import com.zegreatrob.coupling.repository.player.PlayerRepository
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import kotools.types.text.toNotBlankString
import kotlin.test.Test
import kotlin.uuid.Uuid

interface PlayerEmailRepositoryValidator<R> : PlayerRepositoryValidator<R>
    where R : PlayerRepository, R : PlayerListGetByEmail {

    @Test
    fun getPlayersForEmailsWillReturnLatestVersionOfPlayers() = repositorySetup.with(
        object : PartyContextMint<R>() {
            val email = "test-${Uuid.random()}@zegreatrob.com".toNotBlankString().getOrThrow()
            val player = stubPlayer().copy(email = email.toString())
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
        repository.getPlayersByEmail(email)
            .map { it.data }
            .assertIsEqualTo(listOf(partyId.with(updatedPlayer)), "Could not find by email <$email>")
    }

    @Test
    fun getPlayersForEmailsWillIncludeAdditionalEmails() = repositorySetup.with(
        object : PartyContextMint<R>() {
            val email = "test-${Uuid.random()}@zegreatrob.com".toNotBlankString().getOrThrow()
            val player = stubPlayer().copy(email = "alt", additionalEmails = setOf(email.toString()))
            val redHerring = stubPlayer().copy(email = "something else")
        }.bind(),
    ) exercise {
        with(repository) {
            save(partyId.with(player))
            save(partyId.with(redHerring))
        }
    } verifyWithWait {
        repository.getPlayersByEmail(email)
            .map { it.data }
            .assertIsEqualTo(listOf(partyId.with(player)), "Could not find by email <$email>")
    }

    @Test
    fun getPlayersForEmailsWillNotIncludePlayersThatChangedTheirEmailToSomethingElse() = repositorySetup.with(
        object : PartyContextMint<R>() {
            val email = "test-${Uuid.random()}@zegreatrob.com".toNotBlankString().getOrThrow()
            val player = stubPlayer().copy(email = email.toString())
            val updatedPlayer = player.copy(name = "Besto", email = "something else ")
        }.bind(),
    ) exercise {
        repository.save(partyId.with(player))
        repository.save(partyId.with(updatedPlayer))
    } verifyWithWait {
        repository.getPlayersByEmail(email)
            .assertIsEqualTo(emptyList())
    }

    @Test
    fun getPlayersForEmailsWillNotIncludePlayersThatHaveBeenRemoved() = repositorySetup.with(
        object : PartyContextMint<R>() {
            val email = "test-${Uuid.random()}@zegreatrob.com".toNotBlankString().getOrThrow()
            val player = stubPlayer().copy(email = email.toString())
        }.bind(),
    ) exercise {
        repository.save(partyId.with(player))
        repository.deletePlayer(partyId, player.id)
    } verifyWithWait {
        repository.getPlayersByEmail(email)
            .assertIsEqualTo(emptyList())
    }
}
