package com.zegreatrob.coupling.server.action.subscription

import com.zegreatrob.coupling.model.user.UserDetails

interface UserDetailsSubscriptionTrait {
    val subscriptionRepository: SubscriptionRepository
    suspend fun UserDetails.subscriptionDetails() = subscriptionRepository.findSubscriptionDetails(email)
}
