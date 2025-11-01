package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.fire
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.repository.validation.assertHasIds
import com.zegreatrob.coupling.repository.validation.assertIsCloseToNow
import com.zegreatrob.coupling.repository.validation.verifyWithWait
import com.zegreatrob.coupling.sdk.gql.ApolloGraphQuery
import com.zegreatrob.coupling.sdk.schema.PartyPlayersDataQuery
import com.zegreatrob.coupling.sdk.schema.PartyPlayersDetailsQuery
import com.zegreatrob.coupling.sdk.schema.PartyRetiredPlayersDataQuery
import com.zegreatrob.coupling.sdk.schema.PartyRetiredPlayersDetailsQuery
import com.zegreatrob.coupling.sdk.schema.PartySpinsUntilFullRotationQuery
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubPlayers
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.waitForTest
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.uuid.Uuid

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
        sdk().fire(ApolloGraphQuery(PartyPlayersDetailsQuery(party.id)))
            ?.party
            ?.playerList
    } verify { result ->
        result?.map { it.playerDetailsFragment.toModel() }
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
        sdk().fire(ApolloGraphQuery(PartyPlayersDetailsQuery(partyId)))
            ?.party
            ?.playerList
    } verifyWithWait { result ->
        result?.map { it.playerDetailsFragment.toModel() }
            ?.contains(this.player)
            .assertIsEqualTo(false)
    }

    @Test
    fun deleteWithUnknownPlayerIdWillNotExplode() = sdkSetup.with({
        object {
            val sdk = it.sdk
            val party = it.party
            val playerId = PlayerId.new()
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
        sdk().fire(ApolloGraphQuery(PartyRetiredPlayersDetailsQuery(party.id)))
            ?.party
            ?.retiredPlayers
    } verify { result ->
        result?.map { it.playerDetailsFragment.toModel() }
            .assertIsEqualTo(listOf(this.player))
    }

    @Test
    fun multipleDeletedPlayersWithSameEmailWillCollapseIntoOne() = sdkSetup.with(
        {
            object {
                val sdk = it.sdk
                val party = it.party
                val player = stubPlayer()
                val similarPlayer = stubPlayer()
                    .copy(
                        email = player.email,
                        additionalEmails = setOf(Uuid.random().toString()),
                    )
            }
        },
    ) {
        sdk.fire(SavePlayerCommand(party.id, player))
        sdk.fire(DeletePlayerCommand(party.id, player.id))
        sdk.fire(SavePlayerCommand(party.id, similarPlayer))
        sdk.fire(DeletePlayerCommand(party.id, similarPlayer.id))
    } exercise {
        sdk().fire(ApolloGraphQuery(PartyRetiredPlayersDetailsQuery(party.id)))
            ?.party
            ?.retiredPlayers
    } verify { result ->
        result?.map { it.playerDetailsFragment.toModel() }
            .assertIsEqualTo(
                listOf(player.copy(additionalEmails = player.additionalEmails + similarPlayer.additionalEmails)),
            )
    }

    @Test
    fun multipleDeletedPlayersWithBlankEmailWillNotCollapseIntoOne() = sdkSetup.with(
        {
            object {
                val sdk = it.sdk
                val party = it.party
                val player = stubPlayer().copy(email = "")
                val player2 = stubPlayer()
                    .copy(email = " ")
            }
        },
    ) {
        sdk.fire(SavePlayerCommand(party.id, player))
        sdk.fire(DeletePlayerCommand(party.id, player.id))
        sdk.fire(SavePlayerCommand(party.id, player2))
        sdk.fire(DeletePlayerCommand(party.id, player2.id))
    } exercise {
        sdk().fire(ApolloGraphQuery(PartyRetiredPlayersDetailsQuery(party.id)))
            ?.party
            ?.retiredPlayers
    } verify { result ->
        result?.map { it.playerDetailsFragment.toModel() }
            .assertIsEqualTo(listOf(player, player2))
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
        sdk().fire(ApolloGraphQuery(PartyRetiredPlayersDetailsQuery(party.id)))
            ?.party
            ?.retiredPlayers
            ?.map { it.playerDetailsFragment.toModel() }
            .assertIsEqualTo(listOf(this.player))
    }

    @Test
    fun saveMultipleInPartyThenGetListWillReturnSavedPlayers() = sdkSetup.with({
        object : ScopeMint() {
            val sdk = it.sdk
            val partyId = it.party.id
            val players = stubPlayers(3)
        }
    }) {
        players.forEach { setupScope.launch { sdk.fire(SavePlayerCommand(partyId, it)) } }
    } exercise {
        sdk().fire(ApolloGraphQuery(PartyPlayersDetailsQuery(partyId)))
            ?.party
            ?.playerList
    } verify { result ->
        result?.map { it.playerDetailsFragment.toModel() }?.toSet()
            .assertIsEqualTo(players.toSet())
    }

    @Test
    fun canQueryFullRotation() = sdkSetup.with({
        object {
            val sdk = it.sdk
            val partyId = it.party.id
            val players = stubPlayers(4)
        }
    }) {
        coroutineScope {
            players.forEach { launch { sdk.fire(SavePlayerCommand(partyId, it)) } }
        }
    } exercise {
        sdk().fire(ApolloGraphQuery(PartySpinsUntilFullRotationQuery(partyId)))
            ?.party
            ?.spinsUntilFullRotation
    } verify { result ->
        result.assertIsEqualTo(3)
    }

    @Test
    fun saveWorksWithNullableValuesAndAssignsIds() = sdkSetup.with({
        object {
            val sdk = it.sdk
            val party = it.party
            val player = stubPlayer().copy(
                name = "",
                email = "",
                callSignAdjective = "1",
                callSignNoun = "2",
                imageURL = null,
            )
        }
    }) {
        sdk.fire(SavePlayerCommand(party.id, player))
    } exercise {
        sdk().fire(ApolloGraphQuery(PartyPlayersDetailsQuery(party.id)))
            ?.party
            ?.playerList
    } verify { result ->
        result?.map { it.playerDetailsFragment.toModel() }
            .also { it!!.assertHasIds() }
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
        sdk().fire(ApolloGraphQuery(PartyPlayersDetailsQuery(partyId)))
            ?.party
            ?.playerList
    } verifyAnd { result ->
        result?.map { it.playerDetailsFragment.toModel() }
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
        sdk().fire(ApolloGraphQuery(PartyRetiredPlayersDataQuery(party.id)))
            ?.party
            ?.retiredPlayers
    } verify { result ->
        result?.size
            .assertIsEqualTo(1)
        result!!.first().apply {
            isDeleted.assertIsEqualTo(true)
            timestamp.assertIsCloseToNow()
            modifyingUserEmail.assertIsNotEqualTo(null, "As long as an id exists, we're good.")
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
        sdk().fire(ApolloGraphQuery(PartyPlayersDataQuery(party.id)))
            ?.party
            ?.playerList
    } verify { result ->
        result?.size.assertIsEqualTo(1)
        result!!.first().apply {
            timestamp.assertIsCloseToNow()
            modifyingUserEmail.assertIsNotEqualTo(null, "As long as an id exists, we're good.")
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
                    sdk.fire(ApolloGraphQuery(PartyPlayersDetailsQuery(party.id)))
                        ?.party
                } verifyAnd { result ->
                    result.assertIsEqualTo(null)
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
                    val player = stubPlayer().copy(
                        name = "Awesome-O",
                        callSignAdjective = "Awesome",
                        callSignNoun = "Sauce",
                    )
                }) {
                    otherSdk.fire(SavePartyCommand(party))
                } exercise {
                    sdk.fire(SavePlayerCommand(party.id, player))
                    otherSdk.fire(ApolloGraphQuery(PartyPlayersDetailsQuery(party.id)))
                        ?.party
                        ?.playerList
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
            sdk().fire(DeletePlayerCommand(party.id, PlayerId.new()))
        } verify { result ->
            result.assertIsEqualTo(CommandResult.Unauthorized)
        }
    }
}
