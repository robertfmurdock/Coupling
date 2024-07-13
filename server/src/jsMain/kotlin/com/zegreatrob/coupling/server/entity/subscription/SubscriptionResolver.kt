package com.zegreatrob.coupling.server.entity.subscription

import com.zegreatrob.coupling.action.subscription.SubscriptionQuery
import com.zegreatrob.coupling.action.subscription.perform
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.server.graphql.DispatcherProviders
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val subscriptionResolver = dispatch(
    dispatcherFunc = DispatcherProviders.prereleaseCommand(),
    commandFunc = { _: JsonNull, _: JsonNull? -> SubscriptionQuery },
    fireFunc = ::perform,
    toSerializable = { it?.toJson() },
)
