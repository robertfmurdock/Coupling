package com.zegreatrob.coupling.server.pairassignments

import com.zegreatrob.coupling.server.external.graphql.*

val PairAssignmentDocumentType by lazy {
    objectType(
        name = "PairAssignmentDocument",
        description = "Assignments!",
        fields = arrayOf(
            field("_id", GraphQLNonNull(GraphQLString)),
            field("date", GraphQLNonNull(GraphQLString)),
            field("pairs", GraphQLList(PinnedPairType)),
            field("modifyingUserEmail", GraphQLString),
            field("timestamp", GraphQLString),
            field("isDeleted", GraphQLBoolean)
        )
    )
}