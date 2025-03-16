package com.zegreatrob.coupling.sdk.dsl

import com.zegreatrob.coupling.json.GqlAvatarType
import com.zegreatrob.coupling.json.GqlBoostDetails
import com.zegreatrob.coupling.json.GqlContribution
import com.zegreatrob.coupling.json.GqlGlobalStats
import com.zegreatrob.coupling.json.GqlPairAssignmentDocumentDetails
import com.zegreatrob.coupling.json.GqlPartyDetails
import com.zegreatrob.coupling.json.GqlPartyIntegration
import com.zegreatrob.coupling.json.GqlPartySecret
import com.zegreatrob.coupling.json.GqlPartyStats
import com.zegreatrob.coupling.json.GqlPin
import com.zegreatrob.coupling.json.GqlPinDetails
import com.zegreatrob.coupling.json.GqlPinnedPair
import com.zegreatrob.coupling.json.GqlPinnedPlayer
import com.zegreatrob.coupling.json.GqlPlayerDetails
import com.zegreatrob.coupling.json.GqlSubscriptionDetails
import com.zegreatrob.coupling.json.GqlUserDetails
import com.zegreatrob.coupling.model.ContributionId
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.SecretId
import com.zegreatrob.coupling.model.pin.PinId
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.model.user.UserId
import kotlinx.datetime.Instant
import kotools.types.text.toNotBlankString
import kotlin.time.Duration

object GqlReference {
    private val notBlank = "-".toNotBlankString().getOrThrow()
    val globalStats = GqlGlobalStats(
        parties = listOf(
            GqlPartyStats(
                name = "",
                id = PartyId(notBlank),
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
        id = UserId(notBlank),
    )
    val boost = GqlBoostDetails(
        userId = UserId(notBlank),
        partyIds = emptyList(),
        expirationDate = Instant.DISTANT_PAST,
        modifyingUserEmail = notBlank,
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
        id = PinId(notBlank),
        name = "",
        icon = "",
    )
    private val pinnedPlayer = GqlPinnedPlayer(
        id = PlayerId(notBlank),
        name = "",
        email = "",
        badge = "",
        callSignAdjective = "",
        callSignNoun = "",
        imageURL = "",
        avatarType = GqlAvatarType.Retro,
        pins = listOf(pinData),
        unvalidatedEmails = listOf(""),
    )
    val pinnedCouplingPair = GqlPinnedPair(
        players = listOf(pinnedPlayer),
        pins = listOf(pinData),
    )
    val pairAssignmentRecord = GqlPairAssignmentDocumentDetails(
        id = PairAssignmentDocumentId(notBlank),
        date = Instant.DISTANT_PAST,
        pairs = listOf(pinnedCouplingPair),
        partyId = PartyId(notBlank),
        discordMessageId = "",
        slackMessageId = "",
        modifyingUserEmail = notBlank,
        isDeleted = false,
        timestamp = Instant.DISTANT_PAST,
    )
    val integrationRecord = GqlPartyIntegration(
        slackTeam = "",
        slackChannel = "",
        modifyingUserEmail = notBlank,
        isDeleted = false,
        timestamp = Instant.DISTANT_PAST,
    )
    val secretRecord = GqlPartySecret(
        id = SecretId(notBlank),
        partyId = PartyId(notBlank),
        modifyingUserEmail = notBlank,
        isDeleted = false,
        description = "",
        lastUsedTimestamp = Instant.DISTANT_PAST,
        createdTimestamp = Instant.DISTANT_FUTURE,
        timestamp = Instant.DISTANT_PAST,
    )
    val contributionRecord = GqlContribution(
        id = ContributionId(notBlank),
        createdAt = Instant.DISTANT_FUTURE,
        dateTime = Instant.DISTANT_PAST,
        hash = "",
        ease = 99,
        story = "",
        link = "",
        participantEmails = listOf(""),
        partyId = PartyId(notBlank),
        modifyingUserEmail = notBlank,
        isDeleted = false,
        timestamp = Instant.DISTANT_PAST,
        label = "",
        semver = "",
        firstCommit = "",
        firstCommitDateTime = Instant.DISTANT_PAST,
        integrationDateTime = Instant.DISTANT_PAST,
        cycleTime = Duration.ZERO,
        name = "",
        commitCount = 1,
    )
    val partyRecord = GqlPartyDetails(
        id = PartyId(notBlank),
        pairingRule = 0,
        badgesEnabled = false,
        defaultBadgeName = "",
        alternateBadgeName = "",
        email = "",
        name = "",
        callSignsEnabled = false,
        animationsEnabled = false,
        animationSpeed = 0.0,
        modifyingUserEmail = notBlank,
        isDeleted = false,
        timestamp = Instant.DISTANT_PAST,
    )

    val pinRecord = GqlPinDetails(
        id = PinId(notBlank),
        name = "",
        icon = "",
        partyId = PartyId(notBlank),
        modifyingUserEmail = notBlank,
        isDeleted = false,
        timestamp = Instant.DISTANT_PAST,
    )

    val playerRecord = GqlPlayerDetails(
        id = PlayerId(notBlank),
        name = "",
        email = "",
        badge = "",
        callSignAdjective = "",
        callSignNoun = "",
        imageURL = "",
        avatarType = GqlAvatarType.Retro,
        partyId = PartyId(notBlank),
        modifyingUserEmail = notBlank,
        isDeleted = false,
        timestamp = Instant.DISTANT_PAST,
        unvalidatedEmails = emptyList(),
    )
}
