package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.sdk.PlayersTest.Companion.catchError
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlin.test.Test

class TribesTest {
    @Test
    fun postsThenGetWillReturnSavedTribes() = testAsync {
        val sdk = authorizedSdk(username = "eT-user-${uuid4()}")
        setupAsync(object {
            val tribes = listOf(
                KtTribe(TribeId(uuid4().toString()), name = "one", badgesEnabled = true),
                KtTribe(TribeId(uuid4().toString()), name = "two", callSignsEnabled = true),
                KtTribe(TribeId(uuid4().toString()), name = "three")
            )
        }) {
            tribes.forEach { sdk.save(it) }
        } exerciseAsync {
            sdk.getTribesAsync().await()
        } verifyAsync { result ->
            result.assertIsEqualTo(tribes)
        }
    }

    @Test
    fun getWillReturnAnyTribeThatHasPlayerWithGivenEmail() = testAsync {
        val otherSdk = authorizedSdk(username = "eT-other-user-${uuid4()}")
        val username = "eT-user-${uuid4()}"
        val sdk = authorizedSdk(username = username)
        setupAsync(object {
            val tribe = KtTribe(TribeId(uuid4().toString()), name = "tribe-from-endpoint-tests")
            val player = Player(
                id = monk.id().toString(),
                name = "delete-me",
                email = "$username._temp"
            )
        }) {
            otherSdk.save(tribe)
            otherSdk.save(TribeIdPlayer(tribe.id, player))
        } exerciseAsync {
            sdk.getTribesAsync().await()
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
            val tribe = KtTribe(TribeId(uuid4().toString()), name = "tribe-from-endpoint-tests")
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
            sdk.getTribesAsync().await()
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
            val tribe = KtTribe(TribeId(uuid4().toString()), name = "tribe-from-endpoint-tests")
        }) {
            otherSdk.save(tribe)
        } exerciseAsync {
            catchError {
                sdk.save(tribe)
            }
        } verifyAsync { result ->
            result["status"].assertIsEqualTo(400)
        }
    }

    @Test
    fun deleteWillRemoveTribeFromRegularCommunications() = testAsync {
        val sdk = authorizedSdk("eT-user-${uuid4()}")
        setupAsync(object {
            val tribe = KtTribe(TribeId(uuid4().toString()), name = "tribe-from-endpoint-tests")
        }) {
            sdk.save(tribe)
        } exerciseAsync {
            sdk.delete(tribe.id)
            Pair(
                sdk.getTribesAsync().await(),
                catchError { sdk.getTribeAsync(tribe.id).await() }
            )
        } verifyAsync { (result, error) ->
            result.assertIsEqualTo(emptyList())
            error["status"].assertIsEqualTo(404)
        }
    }
}