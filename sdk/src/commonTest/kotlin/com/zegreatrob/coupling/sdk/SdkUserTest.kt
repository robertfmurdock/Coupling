package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.fire
import com.zegreatrob.coupling.action.user.ConnectUserCommand
import com.zegreatrob.coupling.action.user.CreateConnectUserSecretCommand
import com.zegreatrob.coupling.action.user.DisconnectUserCommand
import com.zegreatrob.coupling.action.user.fire
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.mapper.toDomain
import com.zegreatrob.coupling.sdk.schema.PartyDetailsAndListQuery
import com.zegreatrob.coupling.sdk.schema.UserDetailsQuery
import com.zegreatrob.coupling.sdk.schema.UserPlayersQuery
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotools.types.text.toNotBlankString
import kotlin.test.Test

class SdkUserTest {

    @Test
    fun canPerformUserQuery() = asyncSetup() exercise {
        sdk().fire(GqlQuery(UserDetailsQuery()))
    } verify { result ->
        result?.user?.userDetails.let {
            it?.email.toString().assertIsEqualTo(PRIMARY_AUTHORIZED_USER_EMAIL)
            it?.id.assertIsNotEqualTo(null)
            it?.authorizedPartyIds.assertIsNotEqualTo(null)
        }
    }

    @Test
    fun userQueryCanIncludeAllAssociatedPlayers() = asyncSetup(object {
        val party = stubPartyDetails()
        val player = stubPlayer().copy(email = PRIMARY_AUTHORIZED_USER_EMAIL)
    }) {
        sdk().fire(SavePartyCommand(party))
        sdk().fire(SavePlayerCommand(party.id, player))
    } exercise {
        sdk().fire(GqlQuery(UserPlayersQuery()))
    } verify { result ->
        (result?.user?.players ?: emptyList())
            .map { it.playerDetails.toDomain() }
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
        sdk().fire(GqlQuery(UserDetailsQuery()))
    } verify { result ->
        createResult.assertIsNotEqualTo(null)
        createResult?.let { (secret) ->
            result?.user?.userDetails?.connectSecretId
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
        sdk().fire(GqlQuery(UserDetailsQuery()))
            ?.user?.userDetails?.connectedEmails?.map { it.toString() }
            ?.contains(ALT_AUTHORIZED_USER_EMAIL)
            .assertIsEqualTo(false, "Expected $ALT_AUTHORIZED_USER_EMAIL to not be connected.")
    }

    @Test
    fun canConnectUser() = asyncSetup(object {
        lateinit var token: String
    }) {
        token = sdk().fire(CreateConnectUserSecretCommand)?.second ?: ""
    } exercise {
        altAuthorizedSdkDeferred.await().fire(ConnectUserCommand(token))
    } verifyAnd { result: Boolean? ->
        result.assertIsEqualTo(true)
        altAuthorizedSdkDeferred.await().fire(GqlQuery(UserDetailsQuery()))
            ?.user?.userDetails?.connectedEmails?.map { it.toString() }
            ?.contains(PRIMARY_AUTHORIZED_USER_EMAIL)
            .assertIsEqualTo(true, "Expected $PRIMARY_AUTHORIZED_USER_EMAIL to be connected.")
        sdk().fire(GqlQuery(UserDetailsQuery()))
            ?.user?.userDetails?.connectedEmails?.map { it.toString() }
            ?.contains(ALT_AUTHORIZED_USER_EMAIL)
            .assertIsEqualTo(true, "Expected $ALT_AUTHORIZED_USER_EMAIL to be connected.")
    } teardown {
        sdk().fire(DisconnectUserCommand(ALT_AUTHORIZED_USER_EMAIL.toNotBlankString().getOrThrow()))
    }

    @Test
    fun canDisconnectUser() = asyncSetup(object {
        lateinit var token: String
    }) {
        token = sdk().fire(CreateConnectUserSecretCommand)?.second ?: ""
        altAuthorizedSdkDeferred.await().fire(ConnectUserCommand(token))
    } exercise {
        sdk().fire(DisconnectUserCommand(ALT_AUTHORIZED_USER_EMAIL.toNotBlankString().getOrThrow()))
    } verify {
        altAuthorizedSdkDeferred.await().fire(GqlQuery(UserDetailsQuery()))
            ?.user?.userDetails?.connectedEmails?.map { it.toString() }
            ?.contains(PRIMARY_AUTHORIZED_USER_EMAIL)
            .assertIsEqualTo(false, "Expected $PRIMARY_AUTHORIZED_USER_EMAIL not to be connected.")
        sdk().fire(GqlQuery(UserDetailsQuery()))
            ?.user?.userDetails?.connectedEmails?.map { it.toString() }
            ?.contains(ALT_AUTHORIZED_USER_EMAIL)
            .assertIsEqualTo(false, "Expected $ALT_AUTHORIZED_USER_EMAIL not to be connected.")
    }

    @Test
    fun afterConnectionCanSeePartiesAssociatedWithConnectedUser() = asyncSetup(object {
        val party = stubPartyDetails()
    }) {
        sdk().fire(SavePartyCommand(party))
        val token = sdk().fire(CreateConnectUserSecretCommand)?.second ?: ""
        altAuthorizedSdkDeferred.await().fire(ConnectUserCommand(token))
    } exercise {
        altAuthorizedSdkDeferred.await().fire(
            GqlQuery(PartyDetailsAndListQuery(party.id)),
        )
    } verifyAnd { result ->
        (result?.partyList?.map { it.partyDetails.toDomain() } ?: emptyList())
            .assertContains(party)
        result?.party?.partyDetails?.toDomain()
            .assertIsEqualTo(party)
    } teardown {
        sdk().fire(DisconnectUserCommand(ALT_AUTHORIZED_USER_EMAIL.toNotBlankString().getOrThrow()))
    }

    @Test
    fun connectionTokenCannotBeRedeemedByAnotherAccount() = asyncSetup(object {
        lateinit var token: String
    }) {
        prepareConnectionAccounts()
        token = sdk().fire(CreateConnectUserSecretCommand)!!.second
        altAuthorizedSdkDeferred.await().fire(ConnectUserCommand(token)).assertIsEqualTo(true)
    } exercise {
        connectionAuthorizedSdkDeferred.await().fire(ConnectUserCommand(token))
    } verifyAnd { result ->
        result.assertIsEqualTo(false)
        assertConnections(ALT_AUTHORIZED_USER_EMAIL)
    } teardown { clearConnectionAccounts() }

    @Test
    fun connectionTokenCannotBeReplayedAfterDisconnect() = asyncSetup(object {
        lateinit var token: String
    }) {
        prepareConnectionAccounts()
        token = sdk().fire(CreateConnectUserSecretCommand)!!.second
        altAuthorizedSdkDeferred.await().fire(ConnectUserCommand(token)).assertIsEqualTo(true)
        sdk().fire(DisconnectUserCommand(ALT_AUTHORIZED_USER_EMAIL.toNotBlankString().getOrThrow()))
    } exercise {
        altAuthorizedSdkDeferred.await().fire(ConnectUserCommand(token))
    } verifyAnd { result ->
        result.assertIsEqualTo(false)
        assertConnections(null)
    } teardown { clearConnectionAccounts() }

    @Test
    fun concurrentConnectionRedemptionHasOneSymmetricWinner() = asyncSetup(object {
        lateinit var token: String
    }) {
        prepareConnectionAccounts()
        token = sdk().fire(CreateConnectUserSecretCommand)!!.second
    } exercise {
        coroutineScope {
            val first = async { altAuthorizedSdkDeferred.await().fire(ConnectUserCommand(token)) }
            val second = async { connectionAuthorizedSdkDeferred.await().fire(ConnectUserCommand(token)) }
            first.await() to second.await()
        }
    } verifyAnd { (first, second) ->
        listOf(first, second).count { it == true }.assertIsEqualTo(1)
        listOf(first, second).count { it == false }.assertIsEqualTo(1)
        assertConnections(if (first == true) ALT_AUTHORIZED_USER_EMAIL else CONNECTION_TEST_USER_EMAIL)
    } teardown { clearConnectionAccounts() }

    @Test
    fun selfLinkIsRejectedWithoutConsumingConnectionToken() = asyncSetup(object {
        lateinit var token: String
        lateinit var secret: Secret
    }) {
        prepareConnectionAccounts()
        val created = sdk().fire(CreateConnectUserSecretCommand)!!
        secret = created.first
        token = created.second
    } exercise {
        val selfResult = sdk().fire(ConnectUserCommand(token))
        val pending = sdk().fire(GqlQuery(UserDetailsQuery()))?.user?.userDetails?.connectSecretId
        val authorizedResult = altAuthorizedSdkDeferred.await().fire(ConnectUserCommand(token))
        Triple(selfResult, pending, authorizedResult)
    } verifyAnd { (selfResult, pending, authorizedResult) ->
        selfResult.assertIsEqualTo(false)
        pending.assertIsEqualTo(secret.id)
        authorizedResult.assertIsEqualTo(true)
        assertConnections(ALT_AUTHORIZED_USER_EMAIL)
    } teardown { clearConnectionAccounts() }

    @Test
    fun malformedConnectionTokenIsRejectedWithoutChangingConnections() = asyncSetup() exercise {
        sdk().fire(ConnectUserCommand("invalid-connection-token"))
    } verify { result ->
        result.assertIsEqualTo(false)
    }

    private suspend fun prepareConnectionAccounts() {
        sdk()
        altAuthorizedSdkDeferred.await()
        connectionAuthorizedSdkDeferred.await().fire(GqlQuery(UserDetailsQuery()))
            ?.user?.userDetails?.email.toString().assertIsEqualTo(CONNECTION_TEST_USER_EMAIL)
        clearConnectionAccounts()
        assertConnections(null)
    }

    private suspend fun clearConnectionAccounts() {
        sdk().fire(DisconnectUserCommand(ALT_AUTHORIZED_USER_EMAIL.toNotBlankString().getOrThrow()))
        sdk().fire(DisconnectUserCommand(CONNECTION_TEST_USER_EMAIL.toNotBlankString().getOrThrow()))
        altAuthorizedSdkDeferred.await().fire(DisconnectUserCommand(CONNECTION_TEST_USER_EMAIL.toNotBlankString().getOrThrow()))
    }

    private suspend fun assertConnections(winner: String?) {
        val primary = sdk().fire(GqlQuery(UserDetailsQuery()))?.user?.userDetails
        if (winner != null) primary?.connectSecretId.assertIsEqualTo(null)
        val alternate = altAuthorizedSdkDeferred.await().fire(GqlQuery(UserDetailsQuery()))?.user?.userDetails
        val third = connectionAuthorizedSdkDeferred.await().fire(GqlQuery(UserDetailsQuery()))?.user?.userDetails
        primary?.connectedEmails?.map { it.toString() }?.toSet()
            .assertIsEqualTo(setOfNotNull(winner))
        alternate?.connectedEmails?.map { it.toString() }?.toSet()
            .assertIsEqualTo(if (winner == ALT_AUTHORIZED_USER_EMAIL) setOf(PRIMARY_AUTHORIZED_USER_EMAIL) else emptySet())
        third?.connectedEmails?.map { it.toString() }?.toSet()
            .assertIsEqualTo(if (winner == CONNECTION_TEST_USER_EMAIL) setOf(PRIMARY_AUTHORIZED_USER_EMAIL) else emptySet())
    }
}
