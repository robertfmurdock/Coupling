package com.zegreatrob.coupling.sdk.gql

object Mutation {
    val saveContribution by GqlFileLoader
    val clearContributions by GqlFileLoader
    val spin by GqlFileLoader
    val savePin by GqlFileLoader
    val saveBoost by GqlFileLoader
    val deleteBoost by GqlFileLoader
    val saveParty by GqlFileLoader
    val createSecret by GqlFileLoader
    val createConnectUserSecret by GqlFileLoader
    val connectUser by GqlFileLoader
    val saveSlackIntegration by GqlFileLoader
    val deleteSecret by GqlFileLoader
    val savePlayer by GqlFileLoader
    val savePairAssignments by GqlFileLoader
    val deleteParty by GqlFileLoader
    val deletePin by GqlFileLoader
    val deletePairAssignments by GqlFileLoader
    val deletePlayer by GqlFileLoader
    val grantSlackAccess by GqlFileLoader
    val grantDiscordAccess by GqlFileLoader
}
