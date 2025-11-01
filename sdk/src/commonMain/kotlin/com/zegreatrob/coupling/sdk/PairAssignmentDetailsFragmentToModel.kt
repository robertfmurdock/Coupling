package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.toCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.sdk.adapter.toModel
import com.zegreatrob.coupling.sdk.schema.fragment.PairAssignmentDetailsFragment
import kotools.types.collection.toNotEmptyList

fun PairAssignmentDetailsFragment.toModel() = PairAssignmentDocument(
    id = id,
    date = date,
    pairs = pairs.map { pair ->
        pair.players.map { player ->
            com.zegreatrob.coupling.model.player.Player(
                id = player.id,
                badge = player.badge.toModel(),
                name = player.name,
                email = player.email,
                callSignAdjective = player.callSignAdjective,
                callSignNoun = player.callSignNoun,
                imageURL = player.imageURL,
                avatarType = player.avatarType?.toModel(),
                additionalEmails = player.unvalidatedEmails.toSet(),
            )
        }.toCouplingPair()
            .withPins(
                pins = pair.pins.map { pin ->
                    com.zegreatrob.coupling.model.pin.Pin(
                        id = pin.id,
                        name = pin.name,
                        icon = pin.icon,
                    )
                }.toSet(),
            )
    }.toNotEmptyList().getOrThrow(),
    discordMessageId = discordMessageId,
    slackMessageId = slackMessageId,
)
