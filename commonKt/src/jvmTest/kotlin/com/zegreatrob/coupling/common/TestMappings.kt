package com.zegreatrob.coupling.common

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.soywiz.klock.DateFormat
import com.soywiz.klock.parse
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.withPins
import com.zegreatrob.coupling.common.entity.pin.Pin
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.PairingRule
import com.zegreatrob.coupling.common.entity.tribe.TribeId

val mapper = ObjectMapper()

private val dateFormat = DateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")

actual fun loadJsonTribeSetup(fileResource: String): TribeSetup {
    val fileJson = mapper.readTree(TribeSetup::class.java.classLoader.getResource(fileResource))

    val tribeJson = fileJson["tribe"]

    return TribeSetup(
            tribe = KtTribe(
                    name = tribeJson["name"].textValue(),
                    pairingRule = tribeJson["pairingRule"].intValue().let { PairingRule.fromValue(it) },
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

private fun JsonNode.toPinnedCouplingPair() = PinnedCouplingPair(
        players = map { playerNode ->
            playerNode.toPlayer()
                    .withPins(playerNode["pins"].map { pinNode ->
                        pinNode.toPin()
                    })
        }
)

private fun JsonNode.toPin() = Pin(
        _id = this["_id"].textValue(),
        name = this["name"].textValue(),
        tribe = "no, this should not exist here"
)

private fun JsonNode.toPlayer() = Player(
        id = this["_id"].textValue(),
        badge = this["badge"].intValue(),
        name = this["name"].textValue()
)
