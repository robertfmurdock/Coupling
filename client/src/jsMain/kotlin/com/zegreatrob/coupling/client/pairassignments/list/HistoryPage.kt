package com.zegreatrob.coupling.client.pairassignments.list

import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.gql.HistoryPageQuery
import com.zegreatrob.coupling.client.party.toModel
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.toCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.sdk.gql.ApolloGraphQuery
import com.zegreatrob.coupling.sdk.schema.fragment.PairAssignmentDetailsFragment
import com.zegreatrob.coupling.sdk.schema.type.PartyInput
import com.zegreatrob.coupling.sdk.toModel
import js.lazy.Lazy
import kotools.types.collection.toNotEmptyList

@Lazy
val HistoryPage = partyPageFunction { props, partyId ->
    CouplingQuery(
        commander = props.commander,
        query = ApolloGraphQuery(HistoryPageQuery(PartyInput(partyId))),
        key = partyId.value.toString(),
    ) { reload, commandFunc, result ->
        val (party, history) = result.toHistoryData()
            ?: return@CouplingQuery
        History(party, history, Controls(commandFunc, reload))
    }
}

typealias HistoryData = Pair<PartyDetails, List<PairAssignmentDocument>>

fun HistoryPageQuery.Data.toHistoryData(): HistoryData? = party?.let {
    Pair(
        first = it.details?.partyDetailsFragment?.toModel() ?: return@let null,
        second = it.pairAssignmentDocumentList?.map { doc ->
            doc.pairAssignmentDetailsFragment.toModel()
        } ?: return@let null,
    )
}

fun PairAssignmentDetailsFragment.toModel() = PairAssignmentDocument(
    id = id,
    date = date,
    pairs = pairs.map { pair ->
        pair.players.map { player ->
            Player(
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
                    Pin(
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
