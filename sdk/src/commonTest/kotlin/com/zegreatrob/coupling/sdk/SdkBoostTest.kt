package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.DeleteBoostCommand
import com.zegreatrob.coupling.action.NotFoundResult
import com.zegreatrob.coupling.action.SuccessfulResult
import com.zegreatrob.coupling.action.boost.BoostQuery
import com.zegreatrob.coupling.action.boost.SaveBoostCommand
import com.zegreatrob.coupling.action.user.UserQuery
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.validation.verifyWithWait
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.test.Test

class SdkBoostTest {

    private val setupWithUser = asyncTestTemplate(
        sharedSetup = suspend {
            val sdk = sdk()
            val user = sdk.perform(UserQuery())?.let { Record(it, "") }!!.data
            object : SdkApi by sdk {
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
        perform(BoostQuery())
            .assertIsEqualTo(NotFoundResult("Boost"))
    }

    @Test
    fun getBoostWhenThereIsNoneReturnsNull() = setupWithUser {
    } exercise {
        perform(DeleteBoostCommand())
    } verifyWithWait {
        perform(BoostQuery())
            .assertIsEqualTo(NotFoundResult("Boost"))
    }

    @Test
    fun getSavedBoostWillReturnSuccessfully() = setupWithUser.with({
        object : SdkApi by it {
            val userId = it.user.id
            val partyIds = setOf(PartyId("${uuid4()}"), PartyId("${uuid4()}"))
        }
    }) exercise {
        perform(SaveBoostCommand(partyIds))
    } verifyWithWait {
        perform(BoostQuery())
            .let { it as? SuccessfulResult }
            ?.value
            ?.data
            .assertIsEqualTo(
                Boost(
                    userId = userId,
                    partyIds = partyIds,
                ),
            )
    }

    @Test
    fun saveBoostRepeatedlyGetsLatest() = setupWithUser.with({
        object : SdkApi by it {
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
        perform(BoostQuery())
            .let { it as? SuccessfulResult }
            ?.value
            ?.data
            .assertIsEqualTo(
                Boost(
                    userId = userId,
                    partyIds = updatedBoostParties2,
                ),
            )
    }
}
