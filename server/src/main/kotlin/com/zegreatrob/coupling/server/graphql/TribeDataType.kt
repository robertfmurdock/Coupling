package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.server.entity.pairassignment.PairAssignmentDocumentType
import com.zegreatrob.coupling.server.entity.pairassignment.pairAssignmentListResolve
import com.zegreatrob.coupling.server.entity.pin.PinType
import com.zegreatrob.coupling.server.entity.pin.pinListResolve
import com.zegreatrob.coupling.server.entity.player.PlayerType
import com.zegreatrob.coupling.server.entity.player.playerListResolve
import com.zegreatrob.coupling.server.entity.tribe.TribeType
import com.zegreatrob.coupling.server.entity.tribe.tribeResolve
import com.zegreatrob.coupling.server.external.graphql.*

val TribeDataType by lazy {
    objectType(
        name = "TribeData",
        description = "Everything you wanted to know about a tribe but never asked.",
        fields = arrayOf(
            field("id", GraphQLNonNull(GraphQLString)),
            field("tribe", TribeType, resolve = tribeResolve),
            field("pinList", GraphQLList(PinType), pinListResolve),
            field("playerList", GraphQLList(PlayerType), resolve = playerListResolve),
            field(
                "pairAssignmentDocumentList",
                GraphQLList(PairAssignmentDocumentType),
                resolve = pairAssignmentListResolve
            )
        )
    )
}