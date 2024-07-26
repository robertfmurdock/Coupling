package com.zegreatrob.coupling.sdk.dsl

import com.zegreatrob.coupling.json.GqlAvatarType
import com.zegreatrob.coupling.json.GqlBoostDetails
import com.zegreatrob.coupling.json.GqlContribution
import com.zegreatrob.coupling.json.GqlGlobalStats
import com.zegreatrob.coupling.json.GqlPartyDetails
import com.zegreatrob.coupling.json.GqlPartyIntegration
import com.zegreatrob.coupling.json.GqlPartyStats
import com.zegreatrob.coupling.json.GqlPin
import com.zegreatrob.coupling.json.GqlPinDetails
import com.zegreatrob.coupling.json.GqlPinnedPair
import com.zegreatrob.coupling.json.GqlPinnedPlayer
import com.zegreatrob.coupling.json.GqlPlayerDetails
import com.zegreatrob.coupling.json.GqlSubscriptionDetails
import com.zegreatrob.coupling.json.GqlUserDetails
import com.zegreatrob.coupling.json.JsonPairAssignmentDocumentRecord
import com.zegreatrob.coupling.json.JsonSecretRecord
import com.zegreatrob.coupling.model.party.PartyId
import kotlinx.datetime.Instant
import kotools.types.collection.notEmptyListOf
import kotlin.time.Duration

object GqlReference {
    val globalStats = GqlGlobalStats(
        parties = listOf(
            GqlPartyStats(
                name = "",
                id = "",
                playerCount = 0,
                appliedPinCount = 0,
                uniquePinCount = 0,
                spins = 0,
                medianSpinDurationMillis = 0.0,
                medianSpinDuration = null,
            ),
        ),
        totalParties = 0,
        totalSpins = 0,
        totalPlayers = 0,
        totalAppliedPins = 0,
        totalUniquePins = 0,
    )
    val user = GqlUserDetails(
        authorizedPartyIds = emptyList(),
        email = "",
        id = "",
    )
    val boost = GqlBoostDetails(
        userId = "",
        partyIds = emptyList(),
        expirationDate = Instant.DISTANT_PAST,
        modifyingUserEmail = "",
        isDeleted = false,
        timestamp = Instant.DISTANT_FUTURE,
    )
    val subscription = GqlSubscriptionDetails(
        stripeCustomerId = "",
        stripeSubscriptionId = "",
        isActive = false,
        currentPeriodEnd = Instant.DISTANT_FUTURE,
    )

    private val pinData = GqlPin(
        id = "",
        name = "",
        icon = "",
    )
    private val pinnedPlayer = GqlPinnedPlayer(
        id = "",
        name = "",
        email = "",
        badge = "",
        callSignAdjective = "",
        callSignNoun = "",
        imageURL = "",
        avatarType = GqlAvatarType.BoringBeam,
        pins = listOf(pinData),
        unvalidatedEmails = listOf(""),
    )
    val pinnedCouplingPair = GqlPinnedPair(
        players = listOf(pinnedPlayer),
        pins = listOf(pinData),
    )
    val pairAssignmentRecord = JsonPairAssignmentDocumentRecord(
        id = "",
        date = Instant.DISTANT_PAST,
        pairs = notEmptyListOf(pinnedCouplingPair),
        partyId = PartyId(""),
        discordMessageId = "",
        slackMessageId = "",
        modifyingUserEmail = "",
        isDeleted = false,
        timestamp = Instant.DISTANT_PAST,
    )
    val integrationRecord = GqlPartyIntegration(
        slackTeam = "",
        slackChannel = "",
        modifyingUserEmail = "",
        isDeleted = false,
        timestamp = Instant.DISTANT_PAST,
    )
    val secretRecord = JsonSecretRecord(
        id = "",
        partyId = PartyId(""),
        modifyingUserEmail = "",
        isDeleted = false,
        description = "",
        lastUsedTimestamp = Instant.DISTANT_PAST,
        createdTimestamp = Instant.DISTANT_FUTURE,
        timestamp = Instant.DISTANT_PAST,
    )
    val contributionRecord = GqlContribution(
        id = "",
        createdAt = Instant.DISTANT_FUTURE,
        dateTime = Instant.DISTANT_PAST,
        hash = "",
        ease = 99,
        story = "",
        link = "",
        participantEmails = listOf(""),
        partyId = "",
        modifyingUserEmail = "",
        isDeleted = false,
        timestamp = Instant.DISTANT_PAST,
        label = "",
        semver = "",
        firstCommit = "",
        firstCommitDateTime = Instant.DISTANT_PAST,
        integrationDateTime = Instant.DISTANT_PAST,
        cycleTime = Duration.ZERO,
    )
    val partyRecord = GqlPartyDetails(
        id = "",
        pairingRule = 0,
        badgesEnabled = false,
        defaultBadgeName = "",
        alternateBadgeName = "",
        email = "",
        name = "",
        callSignsEnabled = false,
        animationsEnabled = false,
        animationSpeed = 0.0,
        modifyingUserEmail = "",
        isDeleted = false,
        timestamp = Instant.DISTANT_PAST,
    )

    val pinRecord = GqlPinDetails(
        id = "",
        name = "",
        icon = "",
        partyId = "",
        modifyingUserEmail = "",
        isDeleted = false,
        timestamp = Instant.DISTANT_PAST,
    )

    val playerRecord = GqlPlayerDetails(
        id = "",
        name = "",
        email = "",
        badge = "",
        callSignAdjective = "",
        callSignNoun = "",
        imageURL = "",
        avatarType = GqlAvatarType.BoringBeam,
        partyId = "",
        modifyingUserEmail = "",
        isDeleted = false,
        timestamp = Instant.DISTANT_PAST,
        unvalidatedEmails = emptyList(),
    )
}
