package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.server.entity.pin.PinType
import com.zegreatrob.coupling.server.external.graphql.GraphQLList
import com.zegreatrob.coupling.server.external.graphql.GraphQLString
import com.zegreatrob.coupling.server.external.graphql.field
import com.zegreatrob.coupling.server.external.graphql.objectType

val PinnedPlayerType by lazy {
    objectType(
        name = "PinnedPlayer",
        description = "",
        fields = arrayOf(
            field("_id", GraphQLString),
            field("name", GraphQLString),
            field("email", GraphQLString),
            field("badge", GraphQLString),
            field("callSignAdjective", GraphQLString),
            field("callSignNoun", GraphQLString),
            field("imageURL", GraphQLString),
            field("pins", GraphQLList(PinType))
        )
    )
}