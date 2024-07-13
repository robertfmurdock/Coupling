package com.zegreatrob.coupling.server.entity.user

import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.server.action.user.UserQuery
import com.zegreatrob.coupling.server.action.user.perform
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.command
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val userResolve = dispatch(
    dispatcherFunc = command(),
    commandFunc = { _: JsonNull, _: JsonNull? -> UserQuery },
    fireFunc = ::perform,
    toSerializable = ::toJson,
)

private fun toJson(records: UserDetails) = records.let { User(it.id, details = it, subscription = null, boost = null) }.toSerializable()
