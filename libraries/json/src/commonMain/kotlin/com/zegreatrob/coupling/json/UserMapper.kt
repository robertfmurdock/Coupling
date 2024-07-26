package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.user.User

fun User.toSerializable() = GqlUser(
    id = id,
    details = details?.toSerializable(),
    boost = boost?.toSerializable(),
    subscription = subscription?.toJson(),
)

fun GqlUser.toModel() = User(
    id = id,
    details = details?.toModel(),
    boost = boost?.toModelRecord(),
    subscription = subscription?.toModel(),
)
