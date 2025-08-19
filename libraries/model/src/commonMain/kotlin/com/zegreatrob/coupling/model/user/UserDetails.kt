package com.zegreatrob.coupling.model.user

import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.SecretId
import com.zegreatrob.coupling.model.player.Player
import kotools.types.text.NotBlankString
import kotlin.time.Instant

data class User(
    val id: UserId,
    val details: UserDetails?,
    val players: List<PartyRecord<Player>>?,
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
    val id: UserId,
    val email: NotBlankString,
    val connectedEmails: Set<NotBlankString>,
    val authorizedPartyIds: Set<PartyId>,
    val stripeCustomerId: String?,
    var connectSecretId: SecretId?,
)
