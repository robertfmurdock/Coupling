package com.zegreatrob.coupling.repository.validation

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.player.PlayerListGetByEmail
import com.zegreatrob.coupling.repository.player.PlayerRepository
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

interface PlayerEmailRepositoryValidator<T> : PlayerRepositoryValidator<T>
        where T : PlayerRepository, T : PlayerListGetByEmail {

    @Test
    fun getPlayersForEmailsWillReturnLatestVersionOfPlayers() = testRepository { repository, tribeId, _, _ ->
        asyncSetup(object {
            val email = "test-${uuid4()}@zegreatrob.com"
            val player = stubPlayer().copy(email = email)
            val redHerring = stubPlayer().copy(email = "something else")
            val updatedPlayer = player.copy(name = "Besto")
        }) {
            repository.save(tribeId.with(player))
            repository.save(tribeId.with(redHerring))
            repository.save(tribeId.with(updatedPlayer))
        } exercise {
            repository.getPlayerIdsByEmail(email)
        } verify { result ->
            result.assertIsEqualTo(listOf(tribeId.with(player.id)))
        }
    }

    @Test
    fun getPlayersForEmailsWillNotIncludePlayersThatChangedTheirEmailToSomethingElse() =
        testRepository { repository, tribeId, _, _ ->
            asyncSetup(object {
                val email = "test-${uuid4()}@zegreatrob.com"
                val player = stubPlayer().copy(email = email)
                val updatedPlayer = player.copy(name = "Besto", email = "something else ")
            }) {
                repository.save(tribeId.with(player))
                repository.save(tribeId.with(updatedPlayer))
            } exercise {
                repository.getPlayerIdsByEmail(email)
            } verify { result ->
                result.assertIsEqualTo(emptyList())
            }
        }

    @Test
    fun getPlayersForEmailsWillNotIncludePlayersThatHaveBeenRemoved() = testRepository { repository, tribeId, _, _ ->
        asyncSetup(object {
            val email = "test-${uuid4()}@zegreatrob.com"
            val player = stubPlayer().copy(email = email)
        }) {
            repository.save(tribeId.with(player))
            repository.deletePlayer(tribeId, player.id!!)
        } exercise {
            repository.getPlayerIdsByEmail(email)
        } verify { result ->
            result.assertIsEqualTo(emptyList())
        }
    }

}