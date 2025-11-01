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
import com.zegreatrob.coupling.sdk.gql.ApolloGraphQuery
import com.zegreatrob.coupling.sdk.schema.PartyDetailsAndListQuery
import com.zegreatrob.coupling.sdk.schema.UserDetailsQuery
import com.zegreatrob.coupling.sdk.schema.UserPlayersQuery
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import kotools.types.text.toNotBlankString
import kotlin.test.Test

class SdkUserTest {

    @Test
    fun canPerformUserQuery() = asyncSetup() exercise {
        sdk().fire(ApolloGraphQuery(UserDetailsQuery()))
    } verify { result ->
        result?.user?.details?.userDetailsFragment.let {
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
        sdk().fire(ApolloGraphQuery(UserPlayersQuery()))
    } verify { result ->
        (result?.user?.players ?: emptyList())
            .map { it.playerDetailsFragment.toModel() }
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
        sdk().fire(ApolloGraphQuery(UserDetailsQuery()))
    } verify { result ->
        createResult.assertIsNotEqualTo(null)
        createResult?.let { (secret) ->
            result?.user?.details?.userDetailsFragment?.connectSecretId
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
        sdk().fire(ApolloGraphQuery(UserDetailsQuery()))
            ?.user?.details?.userDetailsFragment?.connectedEmails?.map { it.toString() }
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
        altAuthorizedSdkDeferred.await().fire(ApolloGraphQuery(UserDetailsQuery()))
            ?.user?.details?.userDetailsFragment?.connectedEmails?.map { it.toString() }
            ?.contains(PRIMARY_AUTHORIZED_USER_EMAIL)
            .assertIsEqualTo(true, "Expected $PRIMARY_AUTHORIZED_USER_EMAIL to be connected.")
        sdk().fire(ApolloGraphQuery(UserDetailsQuery()))
            ?.user?.details?.userDetailsFragment?.connectedEmails?.map { it.toString() }
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
        altAuthorizedSdkDeferred.await().fire(ApolloGraphQuery(UserDetailsQuery()))
            ?.user?.details?.userDetailsFragment?.connectedEmails?.map { it.toString() }
            ?.contains(PRIMARY_AUTHORIZED_USER_EMAIL)
            .assertIsEqualTo(false, "Expected $PRIMARY_AUTHORIZED_USER_EMAIL not to be connected.")
        sdk().fire(ApolloGraphQuery(UserDetailsQuery()))
            ?.user?.details?.userDetailsFragment?.connectedEmails?.map { it.toString() }
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
            ApolloGraphQuery(PartyDetailsAndListQuery(party.id)),
        )
    } verifyAnd { result ->
        (result?.partyList?.mapNotNull { it.details?.partyDetailsFragment?.toModel() } ?: emptyList())
            .assertContains(party)
        result?.party?.details?.partyDetailsFragment?.toModel()
            .assertIsEqualTo(party)
    } teardown {
        sdk().fire(DisconnectUserCommand(ALT_AUTHORIZED_USER_EMAIL.toNotBlankString().getOrThrow()))
    }
}
