package com.zegreatrob.coupling.server.route

import com.zegreatrob.coupling.server.entity.pairassignment.PairAssignmentDocumentType
import com.zegreatrob.coupling.server.entity.pairassignment.pairAssignmentQueryRoute
import com.zegreatrob.coupling.server.entity.pin.PinType
import com.zegreatrob.coupling.server.entity.pin.performPinListQueryGQL
import com.zegreatrob.coupling.server.entity.player.PlayerType
import com.zegreatrob.coupling.server.entity.tribe.TribeType
import com.zegreatrob.coupling.server.entity.tribe.performTribeQueryGQL
import com.zegreatrob.coupling.server.entity.verifyAuth
import com.zegreatrob.coupling.server.external.graphql.*

val TribeDataType by lazy {
    objectType(
        name = "TribeData",
        description = "Everything you wanted to know about a tribe but never asked.",
        fields = arrayOf(
            field("id", GraphQLNonNull(GraphQLString)),
            field("tribe", TribeType) { entity, _ -> performTribeQueryGQL(entity["id"].toString()) },
            field("pinList", GraphQLList(PinType), verifyAuth { performPinListQueryGQL() }),
            field("playerList", GraphQLList(PlayerType), verifyAuth { performPlayerListQueryGQL() }),
            field("pairAssignmentDocumentList", GraphQLList(PairAssignmentDocumentType), pairAssignmentQueryRoute)
        )
    )
}