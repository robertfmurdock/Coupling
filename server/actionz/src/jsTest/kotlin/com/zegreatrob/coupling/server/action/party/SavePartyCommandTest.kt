package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.PartyIntegration
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Badge
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.repository.party.PartyRepository
import com.zegreatrob.coupling.repository.pin.PinSave
import com.zegreatrob.coupling.repository.player.PlayerListGetByEmail
import com.zegreatrob.coupling.repository.player.PlayerSave
import com.zegreatrob.coupling.repository.user.UserRepository
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubUserDetails
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.Spy
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import kotools.types.text.NotBlankString
import kotlin.test.Test
import kotlin.time.Clock

class SavePartyCommandTest {

    @Test
    fun willSavePlayersToRepository() = asyncSetup(object : ServerSavePartyCommandDispatcher {
        val partyId = PartyId("woo")
        val player = stubPlayer().copy(
            badge = Badge.Default,
            name = "Tim",
            email = "tim@tim.meat",
            callSignAdjective = "Spicy",
            callSignNoun = "Meatball",
            imageURL = "italian.jpg",
            avatarType = null,
        )

        override val currentUser: UserDetails = stubUserDetails().copy(authorizedPartyIds = setOf(partyId))
        override val userId = currentUser.id

        override val playerSaveRepository = PlayerSaverSpy().apply { whenever(PartyElement(partyId, player), Unit) }
        override val pinSaveRepository = PinSaverSpy()
        override val playerRepository = EmptyPlayerRepository
        override val partyRepository = ExistingPartyRepository()
        override val userRepository = EmptyUserRepository
    }) exercise {
        perform(SavePartyCommand(partyId = partyId, players = listOf(player)))
    } verify { result ->
        result.assertIsEqualTo(VoidResult.Accepted)
        playerSaveRepository.spyReceivedValues
            .assertIsEqualTo(listOf(PartyElement(partyId, player)))
    }

    private class PlayerSaverSpy :
        PlayerSave,
        Spy<PartyElement<Player>, Unit> by SpyData() {
        override suspend fun save(partyPlayer: PartyElement<Player>) = spyFunction(partyPlayer)
    }

    private class PinSaverSpy : PinSave {
        override suspend fun save(partyPin: PartyElement<Pin>) = Unit
    }

    private object EmptyPlayerRepository : PlayerListGetByEmail {
        override suspend fun getPlayersByEmail(emails: List<NotBlankString>) = emptyList<com.zegreatrob.coupling.model.PartyRecord<Player>>()
    }

    private object EmptyUserRepository : UserRepository {
        override suspend fun save(user: UserDetails) = Unit
        override suspend fun getUser(): Record<UserDetails>? = null
        override suspend fun getUsersWithEmail(email: NotBlankString) = emptyList<Record<UserDetails>>()
    }

    private class ExistingPartyRepository : PartyRepository {
        override suspend fun getDetails(partyId: PartyId) = Record(
            data = PartyDetails(id = partyId),
            modifyingUserId = null,
            isDeleted = false,
            timestamp = Clock.System.now(),
        )

        override suspend fun getIntegration(partyId: PartyId): Record<PartyIntegration>? = null
        override suspend fun loadParties(partyIds: Set<PartyId>) = emptyList<Record<PartyDetails>>()
        override suspend fun save(party: PartyDetails) = Unit
        override suspend fun save(integration: PartyElement<PartyIntegration>) = Unit
        override suspend fun deleteIt(partyId: PartyId) = false
    }
}
