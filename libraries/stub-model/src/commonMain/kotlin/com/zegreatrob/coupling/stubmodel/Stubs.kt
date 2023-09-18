package com.zegreatrob.coupling.stubmodel

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.PartyIntegration
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinTarget
import com.zegreatrob.coupling.model.player.AvatarType
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.user.UserDetails
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotools.types.collection.notEmptyListOf
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

fun stubParties(number: Int) = generateSequence(::stubPartyDetails).take(number).toList()

var partyCounter = 1

fun stubPartyDetails() = PartyDetails(
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
).also { partyCounter++ }

fun stubPartyIntegration() = PartyIntegration(
    slackTeam = uuidString(),
    slackChannel = uuidString(),
)

private fun stubPairingRule() = PairingRule.entries[partyCounter % PairingRule.entries.size]

fun stubPartyId() = PartyId(uuidString())

fun stubSecret() = Secret(id = uuidString(), description = uuidString(), createdTimestamp = Clock.System.now())

var playerCounter = 1
fun stubPlayer() = Player(
    id = uuidString(),
    badge = 1,
    name = "Tim $playerCounter",
    email = "tim$playerCounter@tim.meat",
    callSignAdjective = "Spicy $playerCounter",
    callSignNoun = "Meatball $playerCounter",
    imageURL = "italian$playerCounter.jpg",
    avatarType = AvatarType.entries.randomOrNull(),
    unvalidatedEmails = setOf(uuidString()),
).also { playerCounter++ }

fun stubPlayers(number: Int) = generateSequence { stubPlayer() }.take(number).toList()

var pinCounter = 1
fun stubPin() = Pin(uuidString(), "pin $pinCounter", "icon time", stubPinTarget())
    .also { pinCounter++ }

var pinTargetCounter = 1
fun stubPinTarget(): PinTarget {
    val index = pinTargetCounter % PinTarget.entries.size
    return PinTarget.entries[index]
        .also { pinTargetCounter++ }
}

fun Instant.roundToMillis(): Instant = Instant.fromEpochMilliseconds(toEpochMilliseconds())

var pairAssignmentDocumentCounter = 1
fun stubPairAssignmentDoc() = PairAssignmentDocument(
    id = stubPairAssignmentId(),
    date = Clock.System.now().plus(pairAssignmentDocumentCounter.minutes).roundToMillis(),
    pairs = notEmptyListOf(
        PinnedCouplingPair(
            notEmptyListOf(stubPlayer().withPins()),
            setOf(stubPin()),
        ),
    ),
    discordMessageId = uuidString(),
    slackMessageId = uuidString(),
).also { pairAssignmentDocumentCounter++ }

fun stubPairAssignmentId() = PairAssignmentDocumentId(uuidString())

fun uuidString() = uuid4().toString()

var userCounter = 1
fun stubUserDetails() = UserDetails(
    id = uuidString(),
    email = "$userCounter-${uuidString()}@gmail.com",
    authorizedPartyIds = setOf(stubPartyId()),
    stripeCustomerId = uuidString(),
).also { userCounter++ }

fun stubPinnedPlayer() = PinnedPlayer(stubPlayer(), listOf(stubPin()))

fun stubPinnedCouplingPair() = PinnedCouplingPair(notEmptyListOf(stubPinnedPlayer()))

fun <E> record(partyId: PartyId, player: E) = Record(
    PartyElement(partyId, player),
    "test",
    false,
    Instant.DISTANT_PAST,
)

fun stubContribution() = Contribution(
    id = uuidString(),
    createdAt = Clock.System.now().minus(2.days),
    dateTime = Clock.System.now().minus(3.days),
    hash = uuidString(),
    ease = 99,
    story = uuidString(),
    link = uuidString(),
    participantEmails = setOf(uuidString(), uuidString()),
    label = uuidString(),
    semver = uuidString(),
    firstCommit = uuidString(),
)
