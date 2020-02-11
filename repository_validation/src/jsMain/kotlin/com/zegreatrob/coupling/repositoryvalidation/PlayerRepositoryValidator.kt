package com.zegreatrob.coupling.repositoryvalidation

import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.PlayerRepository
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.CoroutineScope
import stubPlayer
import stubPlayers
import kotlin.test.Test

interface PlayerRepositoryValidator {

    suspend fun withRepository(handler: suspend (PlayerRepository, TribeId) -> Unit)

    private fun testRepository(block: suspend CoroutineScope.(PlayerRepository, TribeId) -> Any?) = testAsync {
        withRepository { repository, tribeId -> block(repository, tribeId) }
    }

    @Test
    fun saveMultipleInTribeThenGetListWillReturnSavedPlayers() = testRepository { repository, tribeId ->
        setupAsync(object {
            val players = stubPlayers(3)
        }) {
            players.forEach { repository.save(TribeIdPlayer(tribeId, it)) }
        } exerciseAsync {
            repository.getPlayers(tribeId)
        } verifyAsync { result ->
            result.assertIsEqualTo(players)
        }
    }

    @Test
    fun afterSavingPlayerTwiceGetWillReturnOnlyTheUpdatedPlayer() = testRepository { repository, tribeId ->
        setupAsync(object {
            val player = stubPlayer()
            val updatedPlayer = player.copy(name = "Timmy!")
        }) {
            repository.save(TribeIdPlayer(tribeId, player))
        } exerciseAsync {
            repository.save(TribeIdPlayer(tribeId, updatedPlayer))
            repository.getPlayers(tribeId)
        } verifyAsync { result ->
            result.assertIsEqualTo(listOf(updatedPlayer))
        }
    }

}