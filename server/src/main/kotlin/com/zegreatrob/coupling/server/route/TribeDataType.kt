package com.zegreatrob.coupling.server.route

import com.zegreatrob.coupling.server.entity.pairassignment.PairAssignmentDocumentType
import com.zegreatrob.coupling.server.entity.pairassignment.pairAssignmentQueryRoute
import com.zegreatrob.coupling.server.entity.pin.PinType
import com.zegreatrob.coupling.server.entity.pin.pinListQueryRoute
import com.zegreatrob.coupling.server.entity.player.PlayerType
import com.zegreatrob.coupling.server.entity.player.playersQueryRoute
import com.zegreatrob.coupling.server.entity.tribe.TribeType
import com.zegreatrob.coupling.server.entity.tribe.tribeQueryRoute
import com.zegreatrob.coupling.server.external.graphql.*

val TribeDataType by lazy {
    objectType(
        name = "TribeData",
        description = "Everything you wanted to know about a tribe but never asked.",
        fields = arrayOf(
            field("id", GraphQLNonNull(GraphQLString)),
            field("tribe", TribeType, tribeQueryRoute),
            field("pinList", GraphQLList(PinType), pinListQueryRoute),
            field("playerList", GraphQLList(PlayerType), playersQueryRoute),
            field("pairAssignmentDocumentList", GraphQLList(PairAssignmentDocumentType), pairAssignmentQueryRoute)
        )
    )
}