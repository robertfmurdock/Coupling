package com.zegreatrob.coupling.stubmodel

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinTarget
import com.zegreatrob.coupling.model.player.AvatarType
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.user.User
import korlibs.time.DateTime
import korlibs.time.minutes

fun stubParties(number: Int) = generateSequence { stubParty() }.take(number).toList()

var partyCounter = 1

fun stubParty() = Party(
    id = stubPartyId(),
    pairingRule = stubPairingRule(),
    badgesEnabled = partyCounter % 2 == 0,
    defaultBadgeName = "Badgerton",
    alternateBadgeName = "Badgely",
    email = "stuby@stub.edu",
    name = "Stub Party $partyCounter",
    callSignsEnabled = partyCounter % 2 == 1,
    animationEnabled = partyCounter % 2 == 0,
    animationSpeed = partyCounter.toDouble(),
    slackTeam = uuidString(),
    slackChannel = uuidString(),
).also { partyCounter++ }

private fun stubPairingRule() = PairingRule.values()[partyCounter % PairingRule.values().size]

fun stubPartyId() = PartyId(uuidString())

fun stubSecret() = Secret(id = uuidString())

var playerCounter = 1
fun stubPlayer() = Player(
    id = uuidString(),
    badge = 1,
    name = "Tim $playerCounter",
    email = "tim$playerCounter@tim.meat",
    callSignAdjective = "Spicy $playerCounter",
    callSignNoun = "Meatball $playerCounter",
    imageURL = "italian$playerCounter.jpg",
    avatarType = AvatarType.values().randomOrNull(),
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

var pairAssignmentDocumentCounter = 1
fun stubPairAssignmentDoc() = PairAssignmentDocument(
    id = PairAssignmentDocumentId(uuidString()),
    date = DateTime.now().plus(pairAssignmentDocumentCounter.minutes),
    pairs = listOf(
        PinnedCouplingPair(
            listOf(stubPlayer().withPins()),
            setOf(stubPin()),
        ),
    ),
).also { pairAssignmentDocumentCounter++ }

fun uuidString() = uuid4().toString()

var userCounter = 1
fun stubUser() = User(
    id = uuidString(),
    email = "$userCounter-${uuidString()}@gmail.com",
    authorizedPartyIds = setOf(stubPartyId()),
).also { userCounter++ }
