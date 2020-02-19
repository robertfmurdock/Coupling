package com.zegreatrob.coupling.repository.validation

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.player.PlayerListGetByEmail
import com.zegreatrob.coupling.repository.player.PlayerRepository
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import stubPlayer
import kotlin.test.Test

interface PlayerEmailRepositoryValidator<T> : PlayerRepositoryValidator<T>
        where T : PlayerRepository, T : PlayerListGetByEmail {

    @Test
    fun getPlayersForEmailsWillReturnLatestVersionOfPlayers() = testRepository { repository, tribeId, _ ->
        setupAsync(object {
            val email = "test-${uuid4()}@zegreatrob.com"
            val player = stubPlayer().copy(email = email)
            val redHerring = stubPlayer().copy(email = "something else")
            val updatedPlayer = player.copy(name = "Besto")
        }) {
            repository.save(tribeId.with(player))
            repository.save(tribeId.with(redHerring))
            repository.save(tribeId.with(updatedPlayer))
        } exerciseAsync {
            repository.getPlayersByEmail(email)
        } verifyAsync { result ->
            result.assertIsEqualTo(listOf(tribeId.with(updatedPlayer)))
        }
    }

    @Test
    fun getPlayersForEmailsWillNotIncludePlayersThatChangedTheirEmailToSomethingElse() =
        testRepository { repository, tribeId, _ ->
            setupAsync(object {
                val email = "test-${uuid4()}@zegreatrob.com"
                val player = stubPlayer().copy(email = email)
                val updatedPlayer = player.copy(name = "Besto", email = "something else ")
            }) {
                repository.save(tribeId.with(player))
                repository.save(tribeId.with(updatedPlayer))
            } exerciseAsync {
                repository.getPlayersByEmail(email)
            } verifyAsync { result ->
                result.assertIsEqualTo(emptyList())
            }
        }

}