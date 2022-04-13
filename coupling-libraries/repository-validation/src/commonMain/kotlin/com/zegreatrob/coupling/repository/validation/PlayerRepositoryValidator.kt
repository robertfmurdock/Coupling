package com.zegreatrob.coupling.repository.validation

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.soywiz.klock.seconds
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.repository.player.PlayerRepository
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubPlayers
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test

@ExperimentalCoroutinesApi
interface PlayerRepositoryValidator<R : PlayerRepository> : RepositoryValidator<R, PartyContext<R>> {

    @Test
    fun saveMultipleInPartyThenGetListWillReturnSavedPlayers() = repositorySetup.with(object : PartyContextMint<R>() {
        val players = stubPlayers(3)
    }.bind()) {
        partyId.with(players).forEach { repository.save(it) }
    } exercise {
        repository.getPlayers(partyId)
    } verify { result ->
        result.map { it.data.player }
            .assertIsEqualTo(players)
    }

    @Test
    fun saveWorksWithNullableValuesAndAssignsIds() = repositorySetup.with(object : PartyContextMint<R>() {
        val player = Player(
            callSignAdjective = "1",
            callSignNoun = "2",
            name = "",
            email = "",
            imageURL = null
        )
    }.bind()) {
        repository.save(partyId.with(player))
    } exercise {
        repository.getPlayers(partyId)
    } verify { result ->
        result.map { it.data.player }
            .also { it.assertHasIds() }
            .assertIsEqualTo(listOf(player))
    }

    private fun List<Player>.assertHasIds() {
        forEach { player -> player.id.assertIsNotEqualTo(null) }
    }

    @Test
    fun afterSavingPlayerTwiceGetWillReturnOnlyTheUpdatedPlayer() = repositorySetup.with(
        object : PartyContextMint<R>() {
            val player = stubPlayer()
            val updatedPlayer = player.copy(name = "Timmy!")
        }.bind()
    ) {
        repository.save(partyId.with(player))
    } exercise {
        repository.save(partyId.with(updatedPlayer))
        repository.getPlayers(partyId)
    } verify { result ->
        result.map { it.data.player }
            .assertIsEqualTo(listOf(updatedPlayer))
    }

    @Test
    fun whenPlayerIdIsUsedInTwoDifferentPartiesTheyRemainDistinct() = repositorySetup.with(
        object : PartyContextMint<R>() {
            val player1 = stubPlayer()
            val party2 = stubPartyId()
            val player2 = player1.copy(id = player1.id)
        }.bind()
    ) {
        repository.save(partyId.with(player1))
        repository.save(party2.with(player2))
    } exercise {
        repository.getPlayers(partyId)
    } verify { result ->
        result.map { it.data.player }
            .assertIsEqualTo(listOf(player1))
    }

    @Test
    fun deleteWillRemoveAGivenPlayer() = repositorySetup.with(object : PartyContextMint<R>() {
        val player = stubPlayer()
    }.bind()) {
        repository.save(partyId.with(player))
    } exercise {
        repository.deletePlayer(partyId, player.id)
        repository.getPlayers(partyId)
    } verify { result ->
        result.map { it.data.player }
            .contains(player)
            .assertIsEqualTo(false)
    }

    @Test
    fun deletedPlayersShowUpInGetDeleted() = repositorySetup.with(object : PartyContextMint<R>() {
        val player = stubPlayer()
    }.bind()) {
        repository.save(partyId.with(player))
        repository.deletePlayer(partyId, player.id)
    } exercise {
        repository.getDeleted(partyId)
    } verify { result ->
        result.map { it.data.player }
            .assertIsEqualTo(listOf(player))
    }

    @Test
    fun deletedThenBringBackThenDeletedWillShowUpOnceInGetDeleted() =
        repositorySetup.with(object : PartyContextMint<R>() {
            val player = stubPlayer()
            val playerId = player.id
        }.bind()) {
            repository.save(partyId.with(player))
            repository.deletePlayer(partyId, playerId)
            repository.save(partyId.with(player))
            repository.deletePlayer(partyId, playerId)
        } exercise {
            repository.getDeleted(partyId)
        } verify { result ->
            result.map { it.data.player }
                .assertIsEqualTo(listOf(player))
        }

    @Test
    fun deleteWithUnknownPlayerIdWillReturnFalse() = repositorySetup.with(object : PartyContextMint<R>() {
        val playerId = "${uuid4()}"
    }.bind()) exercise {
        repository.deletePlayer(partyId, playerId)
    } verify { result ->
        result.assertIsEqualTo(false)
    }

    @Test
    fun savedPlayersIncludeModificationDateAndUsername() = repositorySetup.with(object : PartyContextMint<R>() {
        val player = stubPlayer()
    }.bind()) {
    } exercise {
        repository.save(partyId.with(player))
        repository.getPlayers(partyId)
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
    fun deletedPlayersIncludeModificationDateAndUsername() = repositorySetup.with(object : PartyContextMint<R>() {
        val player = stubPlayer()
    }.bind()) {
    } exercise {
        repository.save(partyId.with(player))
        repository.deletePlayer(partyId, player.id)
        repository.getDeleted(partyId)
    } verify { result ->
        result.size.assertIsEqualTo(1)
        result.first().apply {
            isDeleted.assertIsEqualTo(true)
            timestamp.assertIsCloseToNow()
            modifyingUserId.assertIsEqualTo(user.email)
        }
    }

}
