package com.zegreatrob.coupling.server.player

import com.zegreatrob.coupling.server.external.graphql.*

val PlayerType by lazy {
    objectType(
        name = "Player",
        description = "Weirdos who want to couple",
        fields = arrayOf(
            field("_id", GraphQLNonNull(GraphQLString)),
            field("name", GraphQLString),
            field("email", GraphQLString),
            field("badge", GraphQLString),
            field("callSignAdjective", GraphQLString),
            field("callSignNoun", GraphQLString),
            field("imageURL", GraphQLString),
            field("modifyingUserEmail", GraphQLString),
            field("timestamp", GraphQLString),
            field("isDeleted", GraphQLBoolean)
        )
    )
}