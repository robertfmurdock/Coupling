@file:JsModule("@graphql-tools/stitch")
@file:Suppress("unused")

package com.zegreatrob.coupling.server.external.graphql.tools.stitch

import com.zegreatrob.coupling.server.external.graphql.GraphQLSchema
import kotlin.js.Json

external fun stitchSchemas(input: Json): GraphQLSchema
