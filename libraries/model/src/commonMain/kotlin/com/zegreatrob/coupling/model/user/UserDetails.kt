package com.zegreatrob.coupling.model.user

import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import kotlinx.datetime.Instant

data class User(
    val id: String,
    val details: UserDetails?,
    val boost: Record<Boost>?,
    val subscription: SubscriptionDetails?,
)

data class SubscriptionDetails(
    val stripeCustomerId: String?,
    val stripeSubscriptionId: String?,
    val isActive: Boolean,
    val currentPeriodEnd: Instant?,
)

data class UserDetails(
    val id: String,
    val email: String,
    val authorizedPartyIds: Set<PartyId>,
    val stripeCustomerId: String?,
)
