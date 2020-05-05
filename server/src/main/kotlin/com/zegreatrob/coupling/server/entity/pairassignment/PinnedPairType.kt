package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.server.entity.pin.PinType
import com.zegreatrob.coupling.server.external.graphql.GraphQLList
import com.zegreatrob.coupling.server.external.graphql.field
import com.zegreatrob.coupling.server.external.graphql.objectType

val PinnedPairType by lazy {
    objectType(
        name = "PinnedPair",
        fields = arrayOf(
            field("players", GraphQLList(PinnedPlayerType)),
            field("pins", GraphQLList(PinType))
        )
    )
}