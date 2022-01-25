package com.zegreatrob.coupling.server.entity.user

import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.server.action.user.UserQuery
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.command
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonElement

val userResolve = dispatch(command, { _, _: JsonElement -> UserQuery }, ::toJson)

private fun toJson(records: User) = records.toSerializable()
