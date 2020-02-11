package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeRepository
import com.zegreatrob.coupling.repositoryvalidation.TribeRepositoryValidator
import com.zegreatrob.coupling.sdk.PlayersTest.Companion.catchAxiosError
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlin.test.Test

class SdkTribeRepositoryTest : TribeRepositoryValidator() {

    override suspend fun withRepository(handler: suspend (TribeRepository) -> Unit) {
        val sdk = authorizedSdk(username = "eT-user-${uuid4()}")
        handler(sdk)
    }

    @Test
    fun getWillReturnAnyTribeThatHasPlayerWithGivenEmail() = testAsync {
        val otherSdk = authorizedSdk(username = "eT-other-user-${uuid4()}")
        val username = "eT-user-${uuid4()}"
        val sdk = authorizedSdk(username = username)
        setupAsync(object {
            val tribe = Tribe(TribeId(uuid4().toString()), name = "tribe-from-endpoint-tests")
            val player = Player(
                id = monk.id().toString(),
                name = "delete-me",
                email = "$username._temp"
            )
        }) {
            otherSdk.save(tribe)
            otherSdk.save(TribeIdPlayer(tribe.id, player))
        } exerciseAsync {
            sdk.getTribes()
        } verifyAsync { result ->
            result.assertIsEqualTo(listOf(tribe))
        }
    }

    @Test
    fun getWillNotReturnTribeIfPlayerHadEmailButThenHadItRemoved() = testAsync {
        val otherSdk = authorizedSdk(username = "eT-other-user-${uuid4()}")
        val username = "eT-user-${uuid4()}"
        val sdk = authorizedSdk(username = username)
        setupAsync(object {
            val tribe = Tribe(TribeId(uuid4().toString()), name = "tribe-from-endpoint-tests")
            val player = Player(
                id = monk.id().toString(),
                name = "delete-me",
                email = "$username._temp"
            )
        }) {
            otherSdk.save(tribe)
            otherSdk.save(TribeIdPlayer(tribe.id, player))
            otherSdk.save(TribeIdPlayer(tribe.id, player.copy(email = "something else")))
        } exerciseAsync {
            sdk.getTribes()
        } verifyAsync { result ->
            result.assertIsEqualTo(emptyList())
        }
    }

    @Test
    fun postWillFailWhenTribeAlreadyExistsForSomeoneElse() = testAsync {
        val otherSdk = authorizedSdk(username = "eT-other-user-${uuid4()}")
        val username = "eT-user-${uuid4()}"
        val sdk = authorizedSdk(username = username)
        setupAsync(object {
            val tribe = Tribe(TribeId(uuid4().toString()), name = "tribe-from-endpoint-tests")
        }) {
            otherSdk.save(tribe)
        } exerciseAsync {
            catchAxiosError {
                sdk.save(tribe)
            }
        } verifyAsync { result ->
            result["status"].assertIsEqualTo(400)
        }
    }

}