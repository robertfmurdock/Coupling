package com.zegreatrob.coupling.stubmodel

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.soywiz.klock.minutes
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinTarget
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.User


fun stubTribes(number: Int) = generateSequence { stubParty() }.take(number).toList()

var tribeCounter = 1

fun stubParty() = Party(
    id = stubPartyId(),
    name = "Stub Tribe $tribeCounter",
    alternateBadgeName = "Badgely",
    badgesEnabled = tribeCounter % 2 == 0,
    callSignsEnabled = tribeCounter % 2 == 1,
    defaultBadgeName = "Badgerton",
    email = "stuby@stub.edu",
    pairingRule = stubPairingRule(),
    animationEnabled = tribeCounter % 2 == 0,
    animationSpeed = tribeCounter.toDouble()
).also { tribeCounter++ }

private fun stubPairingRule() = PairingRule.values()[tribeCounter % PairingRule.values().size]

fun stubPartyId() = PartyId(uuidString())

var playerCounter = 1
fun stubPlayer() = Player(
    id = uuidString(),
    badge = 1,
    name = "Tim $playerCounter",
    callSignAdjective = "Spicy $playerCounter",
    callSignNoun = "Meatball $playerCounter",
    email = "tim$playerCounter@tim.meat",
    imageURL = "italian$playerCounter.jpg"
).also { playerCounter++ }

fun stubPlayers(number: Int) = generateSequence { stubPlayer() }.take(number).toList()

var pinCounter = 1
fun stubPin() = Pin(uuidString(), "pin $pinCounter", "icon time", stubPinTarget())
    .also { pinCounter++ }

var pinTargetCounter = 1
fun stubPinTarget(): PinTarget {
    val index = pinTargetCounter % PinTarget.values().size
    return PinTarget.values()[index]
        .also { pinTargetCounter++ }
}

fun stubSimplePairAssignmentDocument(date: DateTime = DateTime.now()) = PairAssignmentDocumentId(uuidString())
    .let { id ->
        id to stubPairAssignmentDoc().copy(date = date, id = id)
    }

var pairAssignmentDocumentCounter = 1
fun stubPairAssignmentDoc() = PairAssignmentDocument(
    id = PairAssignmentDocumentId(uuidString()),
    date = DateTime.now().plus(pairAssignmentDocumentCounter.minutes),
    pairs = listOf(
        PinnedCouplingPair(
            listOf(
                stubPlayer().withPins()
            ),
            listOf(
                stubPin()
            )
        )
    )
).also { pairAssignmentDocumentCounter++ }

fun uuidString() = uuid4().toString()

var userCounter = 1
fun stubUser() = User(
    id = uuidString(),
    email = "$userCounter-${uuidString()}@gmail.com",
    authorizedPartyIds = setOf(stubPartyId())
).also { userCounter++ }
