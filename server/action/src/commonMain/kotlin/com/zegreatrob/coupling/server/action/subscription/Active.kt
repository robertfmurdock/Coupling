package com.zegreatrob.coupling.server.action.subscription

import com.zegreatrob.coupling.model.user.SubscriptionDetails

fun SubscriptionDetails?.active() = takeIf { it?.isActive == true }
