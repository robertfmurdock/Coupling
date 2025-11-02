package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.gql.PrereleaseUserPageQuery
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.user.SubscriptionDetails
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.toModel
import com.zegreatrob.minreact.nfc
import js.lazy.Lazy

@Lazy
val PrereleaseUserPage by nfc<PageProps> {
    CouplingQuery(
        commander = it.commander,
        query = GqlQuery(PrereleaseUserPageQuery()),
    ) { reload, dispatcher, result ->
        UserConfig(
            user = result.user?.details?.userDetails?.toModel(),
            subscription = result.user?.subscription?.toModel(),
            partyList = result.partyList?.mapNotNull(::partyDetails) ?: emptyList(),
            dispatcher = dispatcher,
            prereleaseUserConfig = PrereleaseUserConfig(
                stripeAdminCode = result.config?.stripeAdminCode ?: return@CouplingQuery,
                stripePurchaseCode = result.config.stripePurchaseCode ?: return@CouplingQuery,
                boost = result.user?.boost?.toModel(),
            ),
            reload = reload,
        )
    }
}

private fun PrereleaseUserPageQuery.Boost.toModel(): Boost = Boost(
    userId = userId,
    partyIds = partyIds.toSet(),
    expirationDate = expirationDate,
)

private fun partyDetails(list: PrereleaseUserPageQuery.PartyList): PartyDetails? = list.partyDetails.toModel()

private fun PrereleaseUserPageQuery.Subscription.toModel(): SubscriptionDetails = SubscriptionDetails(
    stripeCustomerId = stripeCustomerId,
    stripeSubscriptionId = stripeSubscriptionId,
    isActive = isActive,
    currentPeriodEnd = currentPeriodEnd,
)
