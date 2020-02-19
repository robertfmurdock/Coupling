package com.zegreatrob.coupling.repository.validation

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.soywiz.klock.seconds
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.player.PlayerRepository
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.CoroutineScope
import stubPlayer
import stubPlayers
import kotlin.test.Test

interface PlayerRepositoryValidator {

    suspend fun withRepository(handler: suspend (PlayerRepository, TribeId, User) -> Unit)

    private fun testRepository(block: suspend CoroutineScope.(PlayerRepository, TribeId, User) -> Any?) = testAsync {
        withRepository { repository, tribeId, user -> block(repository, tribeId, user) }
    }

    @Test
    fun saveMultipleInTribeThenGetListWillReturnSavedPlayers() = testRepository { repository, tribeId, _ ->
        setupAsync(object {
            val players = stubPlayers(3)
        }) {
            tribeId.with(players).forEach { repository.save(it) }
        } exerciseAsync {
            repository.getPlayers(tribeId)
        } verifyAsync { result ->
            result.map { it.data.player }
                .assertIsEqualTo(players)
        }
    }

    @Test
    fun afterSavingPlayerTwiceGetWillReturnOnlyTheUpdatedPlayer() = testRepository { repository, tribeId, _ ->
        setupAsync(object {
            val player = stubPlayer()
            val updatedPlayer = player.copy(name = "Timmy!")
        }) {
            repository.save(tribeId.with(player))
        } exerciseAsync {
            repository.save(tribeId.with(updatedPlayer))
            repository.getPlayers(tribeId)
        } verifyAsync { result ->
            result.map { it.data.player }
                .assertIsEqualTo(listOf(updatedPlayer))
        }
    }

    @Test
    fun deleteWillRemoveAGivenPlayer() = testRepository { repository, tribeId, _ ->
        setupAsync(object {
            val player = stubPlayer()
        }) {
            repository.save(tribeId.with(player))
        } exerciseAsync {
            repository.deletePlayer(tribeId, player.id!!)
            repository.getPlayers(tribeId)
        } verifyAsync { result ->
            result.map { it.data.player }
                .contains(player)
                .assertIsEqualTo(false)
        }
    }

    @Test
    fun deletedPlayersShowUpInGetDeleted() = testRepository { repository, tribeId, _ ->
        setupAsync(object {
            val player = stubPlayer()
        }) {
            repository.save(tribeId.with(player))
            repository.deletePlayer(tribeId, player.id!!)
        } exerciseAsync {
            repository.getDeleted(tribeId)
        } verifyAsync { result ->
            result.map { it.data.player }
                .assertIsEqualTo(listOf(player))
        }
    }

    @Test
    fun deletedThenBringBackThenDeletedWillShowUpOnceInGetDeleted() = testRepository { repository, tribeId, _ ->
        setupAsync(object {
            val player = stubPlayer()
            val playerId = player.id!!
        }) {
            repository.save(tribeId.with(player))
            repository.deletePlayer(tribeId, playerId)
            repository.save(tribeId.with(player))
            repository.deletePlayer(tribeId, playerId)
        } exerciseAsync {
            repository.getDeleted(tribeId)
        } verifyAsync { result ->
            result.map { it.data.player }
                .assertIsEqualTo(listOf(player))
        }
    }

    @Test
    fun deleteWithUnknownPlayerIdWillReturnFalse() = testRepository { repository, tribeId, _ ->
        setupAsync(object {
            val playerId = "${uuid4()}"
        }) exerciseAsync {
            repository.deletePlayer(tribeId, playerId)
        } verifyAsync { result ->
            result.assertIsEqualTo(false)
        }
    }

    @Test
    fun savedPlayersIncludeModificationDateAndUsername() = testRepository { repository, tribeId, user ->
        setupAsync(object {
            val player = stubPlayer()
        }) exerciseAsync {
            repository.save(tribeId.with(player))
            repository.getPlayers(tribeId)
        } verifyAsync { result ->
            result.size.assertIsEqualTo(1)
            result.first().apply {
                timestamp.isCloseToNow()
                    .assertIsEqualTo(true)
                modifyingUserEmail.assertIsEqualTo(user.email)
            }
        }
    }

    @Test
    fun deletedPlayersIncludeModificationDateAndUsername() = testRepository { repository, tribeId, user ->
        setupAsync(object {
            val player = stubPlayer()
        }) exerciseAsync {
            repository.save(tribeId.with(player))
            repository.deletePlayer(tribeId, player.id!!)
            repository.getDeleted(tribeId)
        } verifyAsync { result ->
            result.size.assertIsEqualTo(1)
            result.first().apply {
                isDeleted.assertIsEqualTo(true)
                timestamp.isCloseToNow()
                    .assertIsEqualTo(true)
                modifyingUserEmail.assertIsEqualTo(user.email)
            }
        }
    }

    private inline fun DateTime.isCloseToNow() = (DateTime.now() - this) < 1.seconds

}