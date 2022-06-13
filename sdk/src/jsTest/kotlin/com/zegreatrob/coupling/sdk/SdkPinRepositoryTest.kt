package com.zegreatrob.coupling.sdk

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PartyContextMint
import com.zegreatrob.coupling.repository.validation.PinRepositoryValidator
import com.zegreatrob.coupling.repository.validation.bind
import com.zegreatrob.coupling.stubmodel.stubParty
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.AsyncMints.asyncSetup
import com.zegreatrob.testmints.async.AsyncMints.asyncTestTemplate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@ExperimentalTime
class SdkPinRepositoryTest : PinRepositoryValidator<SdkPinRepository> {

    override val repositorySetup = asyncTestTemplate<SdkPartyContext<SdkPinRepository>>(sharedSetup = {
        val sdk = authorizedSdk()
        val tribe = stubParty()
        SdkPartyContext(sdk, sdk.pinRepository, tribe.id, MagicClock())
            .apply {
                tribe.save()
            }
    }, sharedTeardown = {
            it.sdk.partyRepository.deleteIt(it.partyId)
        })

    @Test
    fun givenNoAuthGetIsNotAllowed() = asyncSetup.with({
        val sdk = authorizedSdk()
        val otherSdk = altAuthorizedSdkDeferred.await()
        object {
            val otherTribe = stubParty()
            val sdk = sdk
            val otherSdk = otherSdk
        }
    }) {
        otherSdk.partyRepository.save(otherTribe)
        otherSdk.pinRepository.save(otherTribe.id.with(stubPin()))
    } exercise {
        sdk.pinRepository.getPins(otherTribe.id)
    } verifyAnd { result ->
        result.assertIsEqualTo(emptyList())
    } teardown {
        otherSdk.partyRepository.deleteIt(otherTribe.id)
    }

    override fun savedPinsIncludeModificationDateAndUsername() = repositorySetup.with(
        object : PartyContextMint<SdkPinRepository>() {
            val pin = stubPin()
        }.bind()
    ) {
        repository.save(partyId.with(pin))
    } exercise {
        repository.getPins(partyId)
    } verify { result ->
        result.size.assertIsEqualTo(1)
        result.first().apply {
            modifyingUserId.assertIsEqualTo(user.email)
            timestamp.isWithinOneSecondOfNow()
        }
    }
}

fun DateTime.isWithinOneSecondOfNow() {
    val timeSpan = DateTime.now() - this
    (timeSpan.seconds < 1)
        .assertIsEqualTo(true)
}
