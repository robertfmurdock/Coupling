package com.zegreatrob.coupling.action

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.soywiz.klock.DateFormat
import com.soywiz.klock.parse
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.PairingRule
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId

val mapper = ObjectMapper()

private val dateFormat = DateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")

actual fun loadJsonTribeSetup(fileResource: String): TribeSetup {
    val fileJson = mapper.readTree(TribeSetup::class.java.classLoader.getResource(fileResource))

    val tribeJson = fileJson["tribe"]

    return TribeSetup(
        tribe = Tribe(
            name = tribeJson["name"].textValue(),
            pairingRule = tribeJson["pairingRule"].intValue().let {
                PairingRule.fromValue(
                    it
                )
            },
            defaultBadgeName = tribeJson["defaultBadgeName"].textValue(),
            alternateBadgeName = tribeJson["alternateBadgeName"].textValue(),
            id = tribeJson["id"].textValue().let(::TribeId)
        ),
        players = fileJson["players"].map { it.toPlayer() },
        history = fileJson["history"].map {
            PairAssignmentDocument(
                id = it["_id"].textValue().let(::PairAssignmentDocumentId),
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
            .withPins(playerNode["pins"].map { pinNode ->
                pinNode.toPin()
            })
    }
    return PinnedCouplingPair(
        players = players
    )
}

private fun JsonNode.toPin() = Pin(
    _id = this["_id"].textValue(),
    name = this["name"].textValue()
)

private fun JsonNode.toPlayer() = Player(
    id = this["_id"].textValue(),
    badge = this["badge"].intValue(),
    name = this["name"].textValue()
)
