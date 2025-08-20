package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.fire
import com.zegreatrob.coupling.action.user.ConnectUserCommand
import com.zegreatrob.coupling.action.user.CreateConnectUserSecretCommand
import com.zegreatrob.coupling.action.user.fire
import com.zegreatrob.coupling.model.CouplingQueryResult
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import kotlin.test.Test

class SdkUserTest {

    @Test
    fun canPerformUserQuery() = asyncSetup() exercise {
        sdk().fire(graphQuery { user { details() } })
    } verify { result: CouplingQueryResult? ->
        result?.user?.details.let {
            it?.email.toString().assertIsEqualTo(PRIMARY_AUTHORIZED_USER_NAME)
            it?.id.assertIsNotEqualTo(null)
            it?.authorizedPartyIds.assertIsNotEqualTo(null)
        }
    }

    @Test
    fun userQueryCanIncludeAllAssociatedPlayers() = asyncSetup(object {
        val party = stubPartyDetails()
        val player = stubPlayer().copy(email = PRIMARY_AUTHORIZED_USER_NAME)
    }) {
        sdk().fire(SavePartyCommand(party))
        sdk().fire(SavePlayerCommand(party.id, player))
    } exercise {
        sdk().fire(graphQuery { user { players() } })
    } verify { result: CouplingQueryResult? ->
        (result?.user?.players ?: emptyList())
            .elements
            .assertContains(player)
    }

    @Test
    fun canCreateConnectUserSecret() = asyncSetup(object {
    }) exercise {
        sdk().fire(CreateConnectUserSecretCommand)
    } verify { result ->
        result.assertIsNotEqualTo(null)
        result?.let { (secret, token) ->
            token.assertIsNotEqualTo("")
            secret.description.assertIsEqualTo("Single-use user connection secret")
        }
    }

    @Test
    fun newConnectUserSecretHasListedId() = asyncSetup(object {
        var createResult: Pair<Secret, String>? = null
    }) {
        createResult = sdk().fire(CreateConnectUserSecretCommand)
    } exercise {
        sdk().fire(graphQuery { user { details() } })
    } verify { result ->
        createResult.assertIsNotEqualTo(null)
        createResult?.let { (secret) ->
            result?.user?.details?.connectSecretId
                .assertIsEqualTo(secret.id)
        }
    }

    @Test
    fun whenANewerSecretExistsConnectUserSecretCannotBeUsedToConnectUser() = asyncSetup(object {
        lateinit var firstToken: String
    }) {
        firstToken = sdk().fire(CreateConnectUserSecretCommand)?.second ?: ""
    } exercise {
        sdk().fire(CreateConnectUserSecretCommand)
        altAuthorizedSdkDeferred.await().fire(ConnectUserCommand(firstToken))
    } verify { result: Boolean? ->
        result.assertIsEqualTo(false)
        sdk().fire(graphQuery { user { details() } })
            ?.user?.details?.connectedEmails?.map { it.toString() }?.contains(ALT_AUTHORIZED_USER_NAME)
            .assertIsEqualTo(false, "Expected $ALT_AUTHORIZED_USER_NAME to not be connected.")
    }
}
