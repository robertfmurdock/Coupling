package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.server.external.graphql.*

val TribeType by lazy {
    objectType(
        name = "Tribe",
        description = "The people you couple with!",
        fields = arrayOf(
            field("id", GraphQLNonNull(GraphQLString)),
            field("name", GraphQLString),
            field("email", GraphQLString),
            field("pairingRule", GraphQLString),
            field("defaultBadgeName", GraphQLString),
            field("alternateBadgeName", GraphQLString),
            field("badgesEnabled", GraphQLBoolean),
            field("callSignsEnabled", GraphQLBoolean),
            field("animationsEnabled", GraphQLBoolean),
            field("animationSpeed", GraphQLFloat),
            field("modifyingUserEmail", GraphQLString),
            field("timestamp", GraphQLString),
            field("isDeleted", GraphQLBoolean)
        )
    )
}