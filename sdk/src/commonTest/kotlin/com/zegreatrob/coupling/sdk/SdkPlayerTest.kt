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
            object {
                val sdk = authorizedSdk
                val party = stubPartyDetails()
            }.apply {
                sdk.fire(SavePartyCommand(party))
            }
        },
        sharedTeardown = {
            it.sdk.fire(DeletePartyCommand(it.party.id))
        },
    )

    @Test
    fun afterSavingPlayerTwiceGetWillReturnOnlyTheUpdatedPlayer() = sdkSetup.with({
        object {
            val sdk = it.sdk
            val party = it.party
            val player = stubPlayer()
            val updatedPlayer = player.copy(name = "Timmy!")
        }
    }) {
        sdk.fire(SavePlayerCommand(party.id, player))
    } exercise {
        sdk.fire(SavePlayerCommand(party.id, updatedPlayer))
        sdk.fire(graphQuery { party(party.id) { playerList() } })
            ?.party
            ?.playerList
            .let { it ?: emptyList() }
    } verify { result ->
        result.map { it.data.player }
            .assertIsEqualTo(listOf(this.updatedPlayer))
    }

    @Test
    fun deleteWillRemoveAGivenPlayer() = sdkSetup.with({
        object {
            val sdk = it.sdk
            val partyId = it.party.id
            val player = stubPlayer()
        }
    }) {
        sdk.fire(SavePlayerCommand(partyId, player))
    } exercise {
        sdk.fire(DeletePlayerCommand(partyId, player.id))
        sdk.fire(graphQuery { party(partyId) { playerList() } })
            ?.party
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
            val sdk = it.sdk
            val party = it.party
            val playerId = "${uuid4()}"
        }
    }) exercise {
        runCatching { sdk.fire(DeletePlayerCommand(party.id, playerId)) }
    } verify { result ->
        result.exceptionOrNull()
            .assertIsEqualTo(null)
    }

    @Test
    fun deletedPlayersShowUpInGetDeleted() = sdkSetup.with(
        {
            object {
                val sdk = it.sdk
                val party = it.party
                val player = stubPlayer()
            }
        },
    ) {
        sdk.fire(SavePlayerCommand(party.id, player))
        sdk.fire(DeletePlayerCommand(party.id, player.id))
    } exercise {
        sdk.fire(graphQuery { party(party.id) { retiredPlayers() } })
            ?.party
            ?.retiredPlayers
            .let { it ?: emptyList() }
    } verify { result ->
        result.map { it.data.player }
            .assertIsEqualTo(listOf(this.player))
    }

    @Test
    fun deletedThenBringBackThenDeletedWillShowUpOnceInGetDeleted() = sdkSetup.with({
        object {
            val sdk = it.sdk
            val party = it.party
            val player = stubPlayer()
            val playerId = player.id
        }
    }) exercise {
        sdk.fire(SavePlayerCommand(party.id, player))
        sdk.fire(DeletePlayerCommand(party.id, playerId))
        sdk.fire(SavePlayerCommand(party.id, player))
        sdk.fire(DeletePlayerCommand(party.id, playerId))
    } verifyWithWait {
        sdk.fire(graphQuery { party(party.id) { retiredPlayers() } })
            ?.party
            ?.retiredPlayers
            .let { it ?: emptyList() }
            .map { it.data.player }
            .assertIsEqualTo(listOf(this.player))
    }

    @Test
    fun saveMultipleInPartyThenGetListWillReturnSavedPlayers() = sdkSetup.with({
        object {
            val sdk = it.sdk
            val partyId = it.party.id
            val players = stubPlayers(3)
        }
    }) {
        players.forEach { sdk.fire(SavePlayerCommand(partyId, it)) }
    } exercise {
        sdk.fire(graphQuery { party(partyId) { playerList() } })
            ?.party
            ?.playerList
            .let { it ?: emptyList() }
    } verify { result ->
        result.map { it.data.player }
            .assertIsEqualTo(this.players)
    }

    @Test
    fun saveWorksWithNullableValuesAndAssignsIds() = sdkSetup.with({
        object {
            val sdk = it.sdk
            val party = it.party
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
        sdk.fire(SavePlayerCommand(party.id, player))
    } exercise {
        sdk.fire(graphQuery { party(party.id) { playerList() } })
            ?.party
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
            val sdk = it.sdk
            val partyId = it.party.id
            val player1 = stubPlayer()
            val partyId2 = stubPartyId()
            val player2 = player1.copy(id = player1.id)
        }
    }) {
        sdk.fire(SavePartyCommand(stubPartyDetails().copy(id = partyId2)))
        sdk.fire(SavePlayerCommand(partyId, player1))
        sdk.fire(SavePlayerCommand(partyId2, player2))
    } exercise {
        sdk.fire(graphQuery { party(partyId) { playerList() } })
            ?.party
            ?.playerList
            .let { it ?: emptyList() }
    } verifyAnd { result ->
        result.map { it.data.player }
            .assertIsEqualTo(listOf(player1))
    } teardown {
        sdk.fire(DeletePartyCommand(partyId2))
    }

    @Test
    fun deletedPlayersIncludeModificationDateAndUsername() = sdkSetup.with({
        object {
            val sdk = it.sdk
            val party = it.party
            val player = stubPlayer()
        }
    }) exercise {
        sdk.fire(SavePlayerCommand(party.id, player))
        sdk.fire(DeletePlayerCommand(party.id, player.id))
        sdk.fire(graphQuery { party(party.id) { retiredPlayers() } })
            ?.party
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
            val sdk = it.sdk
            val party = it.party
            val player = stubPlayer()
        }
    }) exercise {
        sdk.fire(SavePlayerCommand(party.id, player))
        sdk.fire(graphQuery { party(party.id) { playerList() } })
            ?.party
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
                    otherSdk.fire(SavePartyCommand(party))
                    otherSdk.fire(SavePlayerCommand(party.id, stubPlayer()))
                } exercise {
                    sdk.fire(graphQuery { party(party.id) { playerList() } })
                        ?.party
                        ?.playerList
                        .let { it ?: emptyList() }
                } verifyAnd { result ->
                    result.assertIsEqualTo(emptyList())
                } teardown {
                    otherSdk.fire(DeletePartyCommand(party.id))
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
                    otherSdk.fire(SavePartyCommand(party))
                } exercise {
                    sdk.fire(SavePlayerCommand(party.id, player))
                    otherSdk.fire(graphQuery { party(party.id) { playerList() } })
                        ?.party
                        ?.playerList
                        .let { it ?: emptyList() }
                } verifyAnd { result ->
                    result.assertIsEqualTo(emptyList())
                } teardown {
                    otherSdk.fire(DeletePartyCommand(party.id))
                }
            }
        }

        @Test
        fun deleteIsNotAllowed() = asyncSetup(object {
            val party = stubPartyDetails()
        }) exercise {
            sdk().fire(DeletePlayerCommand(party.id, "player id"))
        } verify { result ->
            result.assertIsEqualTo(CommandResult.Unauthorized)
        }
    }
}
