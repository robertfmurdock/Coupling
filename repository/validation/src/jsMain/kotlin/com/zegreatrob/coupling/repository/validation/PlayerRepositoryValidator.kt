package com.zegreatrob.coupling.repository.validation

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.player.with
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

    @Test
    fun deleteWillRemoveAGivenPlayer() = testRepository { repository, tribeId ->
        setupAsync(object {
            val player = stubPlayer()
        }) {
            repository.save(TribeIdPlayer(tribeId, player))
        } exerciseAsync {
            repository.deletePlayer(tribeId, player.id!!)
            repository.getPlayers(tribeId)
        } verifyAsync { result ->
            result.contains(player).assertIsEqualTo(false)
        }
    }

    @Test
    fun deletedPlayersShowUpInGetDeleted() = testRepository { repository, tribeId ->
        setupAsync(object {
            val player = stubPlayer()
        }) {
            repository.save(TribeIdPlayer(tribeId, player))
            repository.deletePlayer(tribeId, player.id!!)
        } exerciseAsync {
            repository.getDeleted(tribeId)
        } verifyAsync { result ->
            result.assertIsEqualTo(listOf(player))
        }
    }

    @Test
    fun deletedThenBringBackThenDeletedWillShowUpOnceInGetDeleted() = testRepository { repository, tribeId ->
        setupAsync(object {
            val player = stubPlayer()
            val playerId = player.id!!
        }) {
            repository.save(player with tribeId)
            repository.deletePlayer(tribeId, playerId)
            repository.save(player with tribeId)
            repository.deletePlayer(tribeId, playerId)
        } exerciseAsync {
            repository.getDeleted(tribeId)
        } verifyAsync { result ->
            result.assertIsEqualTo(listOf(player))
        }
    }

    @Test
    fun deleteWithUnknownPlayerIdWillReturnFalse() = testRepository { repository, tribeId ->
        setupAsync(object {
            val playerId = "${uuid4()}"
        }) exerciseAsync {
            repository.deletePlayer(tribeId, playerId)
        } verifyAsync { result ->
            result.assertIsEqualTo(false)
        }
    }

}