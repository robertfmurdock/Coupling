package com.zegreatrob.coupling.action

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.soywiz.klock.DateFormat
import com.soywiz.klock.parse
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player

val mapper = ObjectMapper()

private val dateFormat = DateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")

actual fun loadJsonPartySetup(fileResource: String): PartySetup {
    val fileJson = mapper.readTree(PartySetup::class.java.classLoader.getResource(fileResource))

    val partyJson = fileJson["tribe"]

    return PartySetup(
        party = Party(
            name = partyJson["name"].textValue(),
            pairingRule = partyJson["pairingRule"].intValue().let {
                PairingRule.fromValue(
                    it
                )
            },
            defaultBadgeName = partyJson["defaultBadgeName"].textValue(),
            alternateBadgeName = partyJson["alternateBadgeName"].textValue(),
            id = partyJson["id"].textValue().let(::PartyId)
        ),
        players = fileJson["players"].map { it.toPlayer() },
        history = fileJson["history"].map {
            PairAssignmentDocument(
                id = it["id"].textValue().let(::PairAssignmentDocumentId),
                date = it["date"].textValue().let { text -> dateFormat.parse(text).local },
                pairs = it["pairs"].map { pairNode -> pairNode.toPinnedCouplingPair() }
            )
        }
    )
}

private fun JsonNode.toPinnedCouplingPair(): PinnedCouplingPair {
    val playerArray = if (isArray) this else this["players"]

    val players = playerArray.map { playerNode ->
        playerNode.toPlayer()
            .withPins(
                playerNode["pins"].map { pinNode ->
                    pinNode.toPin()
                }
            )
    }
    return PinnedCouplingPair(
        players = players
    )
}

private fun JsonNode.toPin() = Pin(
    id = this["id"].textValue(),
    name = this["name"].textValue()
)

private fun JsonNode.toPlayer() = Player(
    id = this["id"].textValue(),
    badge = this["badge"].intValue(),
    name = this["name"].textValue()
)
