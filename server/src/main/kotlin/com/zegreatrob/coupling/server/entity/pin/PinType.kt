package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.server.external.graphql.*

val PinType by lazy {
    objectType(
        name = "Pin",
        description = "Something to put on your shirt!!",
        fields = arrayOf(
            field("_id", GraphQLNonNull(GraphQLString)),
            field("icon", GraphQLString),
            field("name", GraphQLString),
            field("modifyingUserEmail", GraphQLString),
            field("timestamp", GraphQLString),
            field("isDeleted", GraphQLBoolean)
        )
    )
}