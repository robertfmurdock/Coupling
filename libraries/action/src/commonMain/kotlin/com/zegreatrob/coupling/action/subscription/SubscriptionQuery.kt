package com.zegreatrob.coupling.action.subscription

import com.zegreatrob.coupling.model.user.SubscriptionDetails
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data object SubscriptionQuery {
    interface Dispatcher {
        suspend fun perform(query: SubscriptionQuery): SubscriptionDetails?
    }
}
