package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.DeleteBoostCommand
import com.zegreatrob.coupling.action.boost.SaveBoostCommand
import com.zegreatrob.coupling.action.user.UserQuery
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.validation.verifyWithWait
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.test.Test

class SdkBoostRepositoryTest {

    private val setupWithUser = asyncTestTemplate(
        sharedSetup = suspend {
            val sdk = authorizedSdk()
            val user = sdk.perform(UserQuery())?.let { Record(it, "") }!!.data
            object : BarebonesSdk by sdk {
                val user = user
            }
        },
    )

    @Test
    fun deleteWillMakeBoostNotRecoverableThroughGet() = setupWithUser {
    } exercise {
        perform(SaveBoostCommand(setOf(PartyId("${uuid4()}"), PartyId("${uuid4()}"))))
        perform(DeleteBoostCommand())
    } verifyWithWait {
        get()
            .assertIsEqualTo(null)
    }

    @Test
    fun getBoostWhenThereIsNoneReturnsNull() = setupWithUser {
    } exercise {
        perform(DeleteBoostCommand())
    } verifyWithWait {
        get()
            .assertIsEqualTo(null)
    }

    @Test
    fun getSavedBoostWillReturnSuccessfully() = setupWithUser.with({
        object : BarebonesSdk by it {
            val userId = it.user.id
            val partyIds = setOf(PartyId("${uuid4()}"), PartyId("${uuid4()}"))
        }
    }) exercise {
        perform(SaveBoostCommand(partyIds))
    } verifyWithWait {
        get()?.data
            .assertIsEqualTo(
                Boost(
                    userId = userId,
                    partyIds = partyIds,
                ),
            )
    }

    @Test
    fun saveBoostRepeatedlyGetsLatest() = setupWithUser.with({
        object : BarebonesSdk by it {
            val userId = it.user.id
            val initialBoostParties = setOf(PartyId("${uuid4()}"), PartyId("${uuid4()}"))
            val updatedBoostParties1 = emptySet<PartyId>()
            val updatedBoostParties2 = setOf(PartyId("${uuid4()}"))
        }
    }) exercise {
        perform(SaveBoostCommand(initialBoostParties))
        perform(SaveBoostCommand(updatedBoostParties1))
        perform(SaveBoostCommand(updatedBoostParties2))
    } verifyWithWait {
        get()?.data
            .assertIsEqualTo(
                Boost(
                    userId = userId,
                    partyIds = updatedBoostParties2,
                ),
            )
    }
}
