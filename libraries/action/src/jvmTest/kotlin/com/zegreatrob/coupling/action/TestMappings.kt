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
import com.zegreatrob.coupling.model.pin.PinId
import com.zegreatrob.coupling.model.player.Badge
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.model.player.defaultPlayer
import kotlinx.datetime.Instant
import kotools.types.collection.toNotEmptyList
import kotools.types.text.toNotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi

val mapper = ObjectMapper()

@OptIn(ExperimentalKotoolsTypesApi::class)
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
                id = PairAssignmentDocumentId((it["id"].textValue().toNotBlankString().getOrThrow())),
                date = Instant.parse(it["date"].textValue()),
                pairs = it["pairs"].map { pairNode -> pairNode.toPinnedCouplingPair() }.toNotEmptyList().getOrThrow(),
                null,
            )
        },
    )
}

private fun JsonNode.toPinnedCouplingPair(): PinnedCouplingPair {
    val playerArray = if (isArray) this else this["players"]

    val players = playerArray.map { playerNode ->
        playerNode.toPlayer()
            .withPins(
                playerNode["pins"].mapNotNull { pinNode ->
                    pinNode.toPin()
                },
            )
    }
    return PinnedCouplingPair(
        pinnedPlayers = players.toNotEmptyList().getOrThrow(),
    )
}

private fun JsonNode.toPin(): Pin? {
    return Pin(
        id = this["id"].textValue().toNotBlankString().getOrNull()?.let(::PinId) ?: return null,
        name = this["name"].textValue(),
    )
}

private fun JsonNode.toPlayer() = defaultPlayer.copy(
    id = PlayerId(this["id"].textValue().toNotBlankString().getOrThrow()),
    badge = this["badge"].intValue().let { badge -> Badge.entries.firstOrNull { badge == it.value } }
        ?: defaultPlayer.badge,
    name = this["name"].textValue(),
)
