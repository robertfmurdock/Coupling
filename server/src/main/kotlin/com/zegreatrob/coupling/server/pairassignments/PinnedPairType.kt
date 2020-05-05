package com.zegreatrob.coupling.server.pairassignments

import com.zegreatrob.coupling.server.external.graphql.GraphQLList
import com.zegreatrob.coupling.server.external.graphql.field
import com.zegreatrob.coupling.server.external.graphql.objectType
import com.zegreatrob.coupling.server.entity.pin.PinType

val PinnedPairType by lazy {
    objectType(
        name = "PinnedPair",
        fields = arrayOf(
            field("players", GraphQLList(PinnedPlayerType)),
            field("pins", GraphQLList(PinType))
        )
    )
}