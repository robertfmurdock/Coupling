package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.repository.validation.assertHasIds
import com.zegreatrob.coupling.repository.validation.assertIsCloseToNow
import com.zegreatrob.coupling.repository.validation.verifyWithWait
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubPlayers
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.waitForTest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class SdkPlayerTest {

    private val sdkSetup = asyncSetup.extend(
        sharedSetup = { _ ->
            val authorizedSdk = sdk()
            object : CouplingSdk by authorizedSdk {
                val party = stubPartyDetails()
            }.apply {
                perform(SavePartyCommand(party))
            }
        },
        sharedTeardown = {
            it.perform(DeletePartyCommand(it.party.id))
        },
    )

    @Test
    fun afterSavingPlayerTwiceGetWillReturnOnlyTheUpdatedPlayer() = sdkSetup.with({
        object {
            val sdk = it
            val player = stubPlayer()
            val updatedPlayer = player.copy(name = "Timmy!")
        }
    }) {
        sdk.perform(SavePlayerCommand(sdk.party.id, player))
    } exercise {
        sdk.perform(SavePlayerCommand(sdk.party.id, updatedPlayer))
        sdk.perform(graphQuery { party(sdk.party.id) { playerList() } })
            ?.partyData
            ?.playerList
            .let { it ?: emptyList() }
    } verify { result ->
        result.map { it.data.player }
            .assertIsEqualTo(listOf(this.updatedPlayer))
    }

    @Test
    fun deleteWillRemoveAGivenPlayer() = sdkSetup.with({
        object {
            val sdk = it
            val partyId = it.party.id
            val player = stubPlayer()
        }
    }) {
        sdk.perform(SavePlayerCommand(sdk.party.id, player))
    } exercise {
        sdk.perform(DeletePlayerCommand(partyId, player.id))
        sdk.perform(graphQuery { party(partyId) { playerList() } })
            ?.partyData
            ?.playerList
            .let { it ?: emptyList() }
    } verifyWithWait { result ->
        result.map { it.data.player }
            .contains(this.player)
            .assertIsEqualTo(false)
    }

    @Test
    fun deleteWithUnknownPlayerIdWillNotExplode() = sdkSetup.with({
        object {
            val sdk = it
            val playerId = "${uuid4()}"
        }
    }) exercise {
        runCatching { sdk.perform(DeletePlayerCommand(sdk.party.id, playerId)) }
    } verify { result ->
        result.exceptionOrNull()
            .assertIsEqualTo(null)
    }

    @Test
    fun deletedPlayersShowUpInGetDeleted() = sdkSetup.with(
        {
            object {
                val sdk = it
                val partyId = sdk.party.id
                val player = stubPlayer()
            }
        },
    ) {
        sdk.perform(SavePlayerCommand(sdk.party.id, player))
        sdk.perform(DeletePlayerCommand(partyId, player.id))
    } exercise {
        sdk.perform(graphQuery { party(partyId) { retiredPlayers() } })
            ?.partyData
            ?.retiredPlayers
            .let { it ?: emptyList() }
    } verify { result ->
        result.map { it.data.player }
            .assertIsEqualTo(listOf(this.player))
    }

    @Test
    fun deletedThenBringBackThenDeletedWillShowUpOnceInGetDeleted() = sdkSetup.with({
        object {
            val sdk = it
            val partyId = it.party.id
            val player = stubPlayer()
            val playerId = player.id
        }
    }) exercise {
        sdk.perform(SavePlayerCommand(sdk.party.id, player))
        sdk.perform(DeletePlayerCommand(partyId, playerId))
        sdk.perform(SavePlayerCommand(sdk.party.id, player))
        sdk.perform(DeletePlayerCommand(partyId, playerId))
    } verifyWithWait {
        sdk.perform(graphQuery { party(partyId) { retiredPlayers() } })
            ?.partyData
            ?.retiredPlayers
            .let { it ?: emptyList() }
            .map { it.data.player }
            .assertIsEqualTo(listOf(this.player))
    }

    @Test
    fun saveMultipleInPartyThenGetListWillReturnSavedPlayers() = sdkSetup.with({
        object {
            val sdk = it
            val partyId = it.party.id
            val players = stubPlayers(3)
        }
    }) {
        players.forEach { sdk.perform(SavePlayerCommand(partyId, it)) }
    } exercise {
        sdk.perform(graphQuery { party(partyId) { playerList() } })
            ?.partyData
            ?.playerList
            .let { it ?: emptyList() }
    } verify { result ->
        result.map { it.data.player }
            .assertIsEqualTo(this.players)
    }

    @Test
    fun saveWorksWithNullableValuesAndAssignsIds() = sdkSetup.with({
        object {
            val sdk = it
            val partyId = it.party.id
            val player = Player(
                name = "",
                email = "",
                callSignAdjective = "1",
                callSignNoun = "2",
                imageURL = null,
                avatarType = null,
            )
        }
    }) {
        sdk.perform(SavePlayerCommand(sdk.party.id, player))
    } exercise {
        sdk.perform(graphQuery { party(partyId) { playerList() } })
            ?.partyData
            ?.playerList
            .let { it ?: emptyList() }
    } verify { result ->
        result.map { it.data.player }
            .also { it.assertHasIds() }
            .assertIsEqualTo(listOf(this.player))
    }

    @Test
    fun whenPlayerIdIsUsedInTwoDifferentPartiesTheyRemainDistinct() = sdkSetup.with({
        object {
            val sdk = it
            val partyId = it.party.id
            val player1 = stubPlayer()
            val partyId2 = stubPartyId()
            val player2 = player1.copy(id = player1.id)
        }
    }) {
        sdk.perform(SavePartyCommand(stubPartyDetails().copy(id = partyId2)))
        sdk.perform(SavePlayerCommand(partyId, player1))
        sdk.perform(SavePlayerCommand(partyId2, player2))
    } exercise {
        sdk.perform(graphQuery { party(partyId) { playerList() } })
            ?.partyData
            ?.playerList
            .let { it ?: emptyList() }
    } verifyAnd { result ->
        result.map { it.data.player }
            .assertIsEqualTo(listOf(player1))
    } teardown {
        sdk.perform(DeletePartyCommand(partyId2))
    }

    @Test
    fun deletedPlayersIncludeModificationDateAndUsername() = sdkSetup.with({
        object {
            val sdk = it
            val partyId = it.party.id
            val player = stubPlayer()
        }
    }) exercise {
        sdk.perform(SavePlayerCommand(sdk.party.id, player))
        sdk.perform(DeletePlayerCommand(partyId, player.id))
        sdk.perform(graphQuery { party(partyId) { retiredPlayers() } })
            ?.partyData
            ?.retiredPlayers
            .let { it ?: emptyList() }
    } verify { result ->
        result.size.assertIsEqualTo(1)
        result.first().apply {
            isDeleted.assertIsEqualTo(true)
            timestamp.assertIsCloseToNow()
            modifyingUserId.assertIsNotEqualTo(null, "As long as an id exists, we're good.")
        }
    }

    @Test
    fun savedPlayersIncludeModificationDateAndUsername() = sdkSetup.with({
        object {
            val sdk = it
            val partyId = it.party.id
            val player = stubPlayer()
        }
    }) exercise {
        sdk.perform(SavePlayerCommand(sdk.party.id, player))
        sdk.perform(graphQuery { party(partyId) { playerList() } })
            ?.partyData
            ?.playerList
            .let { it ?: emptyList() }
    } verify { result ->
        result.size.assertIsEqualTo(1)
        result.first().apply {
            timestamp.assertIsCloseToNow()
            modifyingUserId.assertIsNotEqualTo(null, "As long as an id exists, we're good.")
        }
    }

    class GivenUsersWithoutAccess {

        @Test
        fun getIsNotAllowed() = runTest {
            val sdk = sdk()
            val otherSdk = altAuthorizedSdkDeferred.await()
            waitForTest {
                asyncSetup(object {
                    val party = stubPartyDetails()
                }) {
                    otherSdk.perform(SavePartyCommand(party))
                    otherSdk.perform(SavePlayerCommand(party.id, stubPlayer()))
                } exercise {
                    sdk.perform(graphQuery { party(party.id) { playerList() } })
                        ?.partyData
                        ?.playerList
                        .let { it ?: emptyList() }
                } verifyAnd { result ->
                    result.assertIsEqualTo(emptyList())
                } teardown {
                    otherSdk.perform(DeletePartyCommand(party.id))
                }
            }
        }

        @Test
        fun postIsNotAllowed() = runTest {
            val sdk = sdk()
            val otherSdk = altAuthorizedSdkDeferred.await()
            waitForTest {
                asyncSetup(object {
                    val party = stubPartyDetails()
                    val player = Player(
                        id = "${uuid4()}",
                        name = "Awesome-O",
                        callSignAdjective = "Awesome",
                        callSignNoun = "Sauce",
                        avatarType = null,
                    )
                }) {
                    otherSdk.perform(SavePartyCommand(party))
                } exercise {
                    sdk.perform(SavePlayerCommand(party.id, player))
                    otherSdk.perform(graphQuery { party(party.id) { playerList() } })
                        ?.partyData
                        ?.playerList
                        .let { it ?: emptyList() }
                } verifyAnd { result ->
                    result.assertIsEqualTo(emptyList())
                } teardown {
                    otherSdk.perform(DeletePartyCommand(party.id))
                }
            }
        }

        @Test
        fun deleteIsNotAllowed() = asyncSetup(object {
            val party = stubPartyDetails()
        }) exercise {
            sdk().perform(DeletePlayerCommand(party.id, "player id"))
        } verify { result ->
            result.assertIsEqualTo(CommandResult.Unauthorized)
        }
    }
}
