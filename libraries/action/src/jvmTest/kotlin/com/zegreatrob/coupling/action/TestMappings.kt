package com.zegreatrob.coupling.action

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import kotlinx.datetime.toInstant
import kotools.types.collection.toNotEmptyList

val mapper = ObjectMapper()

actual fun loadJsonPartySetup(fileResource: String): PartySetup {
    val fileJson = mapper.readTree(PartySetup::class.java.classLoader.getResource(fileResource))

    val partyJson = fileJson["party"]

    return PartySetup(
        party = PartyDetails(
            id = partyJson["id"].textValue().let(::PartyId),
            pairingRule = partyJson["pairingRule"].intValue().let {
                PairingRule.fromValue(
                    it,
                )
            },
            defaultBadgeName = partyJson["defaultBadgeName"].textValue(),
            alternateBadgeName = partyJson["alternateBadgeName"].textValue(),
            name = partyJson["name"].textValue(),
        ),
        players = fileJson["players"].map { it.toPlayer() },
        history = fileJson["history"].map {
            PairAssignmentDocument(
                id = it["id"].textValue().let(::PairAssignmentDocumentId),
                date = it["date"].textValue().toInstant(),
                pairs = it["pairs"].map { pairNode -> pairNode.toPinnedCouplingPair() }.toNotEmptyList().getOrThrow(),
            )
        },
    )
}

private fun JsonNode.toPinnedCouplingPair(): PinnedCouplingPair {
    val playerArray = if (isArray) this else this["players"]

    val players = playerArray.map { playerNode ->
        playerNode.toPlayer()
            .withPins(
                playerNode["pins"].map { pinNode ->
                    pinNode.toPin()
                },
            )
    }
    return PinnedCouplingPair(
        pinnedPlayers = players,
    )
}

private fun JsonNode.toPin() = Pin(
    id = this["id"].textValue(),
    name = this["name"].textValue(),
)

private fun JsonNode.toPlayer() = Player(
    id = this["id"].textValue(),
    badge = this["badge"].intValue(),
    name = this["name"].textValue(),
    avatarType = null,
)
