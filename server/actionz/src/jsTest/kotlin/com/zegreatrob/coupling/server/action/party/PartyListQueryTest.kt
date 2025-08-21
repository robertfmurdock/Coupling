package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.party.PartyListGet
import com.zegreatrob.coupling.repository.player.PlayerListGetByEmail
import com.zegreatrob.coupling.repository.user.UserGetByEmail
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.stubmodel.stubUserDetails
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotools.types.text.NotBlankString
import kotlin.test.Test
import kotlin.time.Clock

class PartyListQueryTest {

    @Test
    fun canHandleEmpties() = asyncSetup(object : PartyListQuery.Dispatcher {
        override val currentUser = stubUserDetails()
        override val userId = currentUser.id
        override val userRepository = UserGetByEmail { emptyList() }
        override val playerRepository = PlayerListGetByEmail { emptyList() }
        override val partyRepository = PartyListGet { emptyList() }
    }) exercise {
        perform(PartyListQuery)
    } verify { result ->
        result.assertIsEqualTo(PartyListResult(emptyList(), emptyList()))
    }

    @Test
    fun connectedUserCanGrantAccessToPartyAsPlayer() = asyncSetup(object : PartyListQuery.Dispatcher {
        val connectedUser = stubUserDetails()
        override val currentUser = stubUserDetails()
            .copy(connectedEmails = setOf(connectedUser.email))
        override val userId = currentUser.id
        override val userRepository = UserGetByEmail {
            listOfNotNull(
                if (it == connectedUser.email) {
                    connectedUser.inRecord()
                } else {
                    null
                },
            )
        }
        val expectedParty = stubPartyDetails()
        val expectedPartyRecord = expectedParty.inRecord()
        val player = stubPlayer().copy(email = connectedUser.email.toString())
        override val partyRepository = PartyListGet { listOf(expectedPartyRecord) }
        override val playerRepository = PlayerListGetByEmail {
            listOfNotNull(
                if (it.map(NotBlankString::toString).contains(player.email)) {
                    expectedParty.id.with(player).inRecord()
                } else {
                    null
                },
            )
        }
    }) exercise {
        perform(PartyListQuery)
    } verify { result ->
        result.assertIsEqualTo(PartyListResult(emptyList(), listOf(expectedPartyRecord)))
    }

    private fun <T> T.inRecord(): Record<T> = Record(
        data = this,
        modifyingUserId = null,
        isDeleted = false,
        timestamp = Clock.System.now(),
    )
}
