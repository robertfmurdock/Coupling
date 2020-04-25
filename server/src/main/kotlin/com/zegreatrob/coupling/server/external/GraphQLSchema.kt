package com.zegreatrob.coupling.server.external

import com.zegreatrob.coupling.server.entity.Resolvers

@JsModule("routes/graphqlSchema")
@JsNonModule
external val schema: dynamic

fun graphqlSchema() = schema.buildSchema(Resolvers)