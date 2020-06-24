package com.zegreatrob.coupling.repository.validation

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.soywiz.klock.seconds
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.player.PlayerRepository
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubPlayers
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import kotlin.test.Test

interface PlayerRepositoryValidator<R : PlayerRepository> : RepositoryValidator<R, TribeSharedContext<R>> {

    @Test
    fun saveMultipleInTribeThenGetListWillReturnSavedPlayers() = repositorySetup(object : PlayerContextMint<R>() {
        val players = stubPlayers(3)
    }.bind()) {
        tribeId.with(players).forEach { repository.save(it) }
    } exercise {
        repository.getPlayers(tribeId)
    } verify { result ->
        result.map { it.data.player }
            .assertIsEqualTo(players)
    }

    @Test
    fun saveWorksWithNullableValuesAndAssignsIds() = repositorySetup(object : PlayerContextMint<R>() {
        val player = Player(
            id = null,
            callSignAdjective = "1",
            callSignNoun = "2",
            name = "",
            email = "",
            imageURL = null
        )
    }.bind()) {
        repository.save(tribeId.with(player))
    } exercise {
        repository.getPlayers(tribeId)
    } verify { result ->
        result.map { it.data.player }
            .also { it.assertHasIds() }
            .map { it.copy(id = null) }
            .assertIsEqualTo(listOf(player))
    }

    private fun List<Player>.assertHasIds() {
        forEach { player -> player.id.assertIsNotEqualTo(null) }
    }

    @Test
    fun afterSavingPlayerTwiceGetWillReturnOnlyTheUpdatedPlayer() = repositorySetup(object : PlayerContextMint<R>() {
        val player = stubPlayer()
        val updatedPlayer = player.copy(name = "Timmy!")
    }.bind()) {
        repository.save(tribeId.with(player))
    } exercise {
        repository.save(tribeId.with(updatedPlayer))
        repository.getPlayers(tribeId)
    } verify { result ->
        result.map { it.data.player }
            .assertIsEqualTo(listOf(updatedPlayer))
    }

    @Test
    fun deleteWillRemoveAGivenPlayer() = repositorySetup(object : PlayerContextMint<R>() {
        val player = stubPlayer()
    }.bind()) {
        repository.save(tribeId.with(player))
    } exercise {
        repository.deletePlayer(tribeId, player.id!!)
        repository.getPlayers(tribeId)
    } verify { result ->
        result.map { it.data.player }
            .contains(player)
            .assertIsEqualTo(false)
    }

    @Test
    fun deletedPlayersShowUpInGetDeleted() = repositorySetup(object : PlayerContextMint<R>() {
        val player = stubPlayer()
    }.bind()) {
        repository.save(tribeId.with(player))
        repository.deletePlayer(tribeId, player.id!!)
    } exercise {
        repository.getDeleted(tribeId)
    } verify { result ->
        result.map { it.data.player }
            .assertIsEqualTo(listOf(player))
    }

    @Test
    fun deletedThenBringBackThenDeletedWillShowUpOnceInGetDeleted() = repositorySetup(object : PlayerContextMint<R>() {
        val player = stubPlayer()
        val playerId = player.id!!
    }.bind()) {
        repository.save(tribeId.with(player))
        repository.deletePlayer(tribeId, playerId)
        repository.save(tribeId.with(player))
        repository.deletePlayer(tribeId, playerId)
    } exercise {
        repository.getDeleted(tribeId)
    } verify { result ->
        result.map { it.data.player }
            .assertIsEqualTo(listOf(player))
    }

    @Test
    fun deleteWithUnknownPlayerIdWillReturnFalse() = repositorySetup(object : PlayerContextMint<R>() {
        val playerId = "${uuid4()}"
    }.bind()) exercise {
        repository.deletePlayer(tribeId, playerId)
    } verify { result ->
        result.assertIsEqualTo(false)
    }

    @Test
    fun savedPlayersIncludeModificationDateAndUsername() = repositorySetup(object : PlayerContextMint<R>() {
        val player = stubPlayer()
    }.bind()) {
    } exercise {
        repository.save(tribeId.with(player))
        repository.getPlayers(tribeId)
    } verify { result ->
        result.size.assertIsEqualTo(1)
        result.first().apply {
            timestamp.assertIsCloseToNow()
            modifyingUserId.assertIsEqualTo(user.email)
        }
    }

    fun DateTime.assertIsCloseToNow() {
        val distanceFromNow = DateTime.now() - this
        (distanceFromNow < 5.seconds)
            .assertIsEqualTo(true, "Distance from now was $distanceFromNow, but was expected to be < 5")
    }

    @Test
    fun deletedPlayersIncludeModificationDateAndUsername() = repositorySetup(object : PlayerContextMint<R>() {
        val player = stubPlayer()
    }.bind()) {
    } exercise {
        repository.save(tribeId.with(player))
        repository.deletePlayer(tribeId, player.id!!)
        repository.getDeleted(tribeId)
    } verify { result ->
        result.size.assertIsEqualTo(1)
        result.first().apply {
            isDeleted.assertIsEqualTo(true)
            timestamp.assertIsCloseToNow()
            modifyingUserId.assertIsEqualTo(user.email)
        }
    }

}
