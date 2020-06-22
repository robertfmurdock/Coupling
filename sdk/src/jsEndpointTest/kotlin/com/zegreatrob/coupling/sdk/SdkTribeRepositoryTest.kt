package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.validation.*
import com.zegreatrob.coupling.sdk.SdkPlayerRepositoryTest.Companion.catchAxiosError
import com.zegreatrob.coupling.stubmodel.stubTribe
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlin.test.Test

typealias SdkMint = ContextMint<Sdk>

class SdkTribeRepositoryTest : TribeRepositoryValidator<Sdk> {

    override val repositorySetup = asyncTestTemplate<SharedContext<Sdk>>(sharedSetup = {
        val clock = MagicClock()
        val email = "eT-user-${uuid4()}"
        val sdk = authorizedSdk(username = email)
        SharedContextData(sdk, clock, stubUser().copy(email = "$email._temp"))
    })

    @Test
    fun getWillReturnAnyTribeThatHasPlayerWithGivenEmail() = asyncSetup(contextProvider = {
        val otherSdk = authorizedSdk(username = "eT-other-user-${uuid4()}")
        val username = "eT-user-${uuid4()}"
        val sdk = authorizedSdk(username = username)
        object {
            val sdk = sdk
            val otherSdk = otherSdk
            val tribe = Tribe(TribeId(uuid4().toString()), name = "tribe-from-endpoint-tests")
            val player = Player(
                id = monk.id().toString(),
                name = "delete-me",
                email = "${username}._temp"
            )
        }
    }) {
        otherSdk.save(tribe)
        otherSdk.save(tribe.id.with(player))
    } exercise {
        sdk.getTribes()
    } verify { result ->
        result.map { it.data }
            .assertIsEqualTo(listOf(tribe))
    }

    @Test
    fun getWillNotReturnTribeIfPlayerHadEmailButThenHadItRemoved() = asyncSetup(contextProvider = {
        val otherSdk = authorizedSdk(username = "eT-other-user-${uuid4()}")
        val username = "eT-user-${uuid4()}"
        val sdk = authorizedSdk(username = username)
        object {
            val otherSdk = otherSdk
            val sdk = sdk
            val tribe = Tribe(TribeId(uuid4().toString()), name = "tribe-from-endpoint-tests")
            val player = Player(
                id = monk.id().toString(),
                name = "delete-me",
                email = "$username._temp"
            )
        }
    }) {
        otherSdk.save(tribe)
        otherSdk.save(tribe.id.with(player))
        otherSdk.save(tribe.id.with(player.copy(email = "something else")))
    } exercise {
        sdk.getTribes()
    } verify { result ->
        result.assertIsEqualTo(emptyList())
    }

    @Test
    fun getWillNotReturnTribeIfPlayerHadEmailButPlayerWasRemoved() = asyncSetup(contextProvider = {
        val otherSdk = authorizedSdk(username = "eT-other-user-${uuid4()}")
        val username = "eT-user-${uuid4()}"
        val sdk = authorizedSdk(username = username)
        object {
            val sdk = sdk
            val otherSdk = otherSdk
            val tribe = Tribe(TribeId(uuid4().toString()), name = "tribe-from-endpoint-tests")
            val player = Player(
                id = monk.id().toString(),
                name = "delete-me",
                email = "$username._temp"
            )
        }
    }) {
        otherSdk.save(tribe)
        otherSdk.save(tribe.id.with(player))
        otherSdk.deletePlayer(tribe.id, player.id!!)
    } exercise {
        sdk.getTribes()
    } verify { result ->
        result.assertIsEqualTo(emptyList())
    }

    @Test
    fun postWillFailWhenTribeAlreadyExistsForSomeoneElse() = asyncSetup(contextProvider = {
        val otherSdk = authorizedSdk(username = "eT-other-user-${uuid4()}")
        val username = "eT-user-${uuid4()}"
        val sdk = authorizedSdk(username = username)
        object {
            val sdk = sdk
            val otherSdk = otherSdk
            val tribe = Tribe(TribeId(uuid4().toString()), name = "tribe-from-endpoint-tests")
        }
    }) {
        otherSdk.save(tribe)
    } exercise {
        catchAxiosError { sdk.save(tribe) }
    } verify { result ->
        result["status"].assertIsEqualTo(403)
    }

    override fun saveWillIncludeModificationInformation() = repositorySetup(object : SdkMint() {
        val tribe = stubTribe()
    }.bind()) {
        repository.save(tribe)
    } exercise {
        repository.getTribes()
    } verify { result ->
        result.first { it.data.id == tribe.id }.apply {
            modifyingUserId.assertIsEqualTo(user.email)
            timestamp.isWithinOneSecondOfNow()
        }
    }

}