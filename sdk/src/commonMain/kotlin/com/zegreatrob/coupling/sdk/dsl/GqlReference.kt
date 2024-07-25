package com.zegreatrob.coupling.sdk.dsl

import com.zegreatrob.coupling.json.GqlPin
import com.zegreatrob.coupling.json.JsonBoostRecord
import com.zegreatrob.coupling.json.JsonContributionRecord
import com.zegreatrob.coupling.json.JsonGlobalStats
import com.zegreatrob.coupling.json.JsonIntegrationRecord
import com.zegreatrob.coupling.json.JsonPairAssignmentDocumentRecord
import com.zegreatrob.coupling.json.JsonPartyDetailsRecord
import com.zegreatrob.coupling.json.JsonPartyStats
import com.zegreatrob.coupling.json.JsonPinRecord
import com.zegreatrob.coupling.json.JsonPinnedCouplingPair
import com.zegreatrob.coupling.json.JsonPinnedPlayer
import com.zegreatrob.coupling.json.JsonPlayerRecord
import com.zegreatrob.coupling.json.JsonSecretRecord
import com.zegreatrob.coupling.json.JsonSubscriptionDetails
import com.zegreatrob.coupling.json.JsonUserDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.AvatarType
import kotlinx.datetime.Instant
import kotools.types.collection.notEmptyListOf
import kotlin.time.Duration

object GqlReference {
    val globalStats = JsonGlobalStats(
        parties = listOf(
            JsonPartyStats(
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
    val user = JsonUserDetails(
        authorizedPartyIds = emptySet(),
        email = "",
        id = "",
    )
    val boost = JsonBoostRecord(
        userId = "",
        partyIds = emptySet(),
        expirationDate = Instant.DISTANT_PAST,
        modifyingUserEmail = "",
        isDeleted = false,
        timestamp = Instant.DISTANT_FUTURE,
    )
    val subscription = JsonSubscriptionDetails(
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
    private val pinnedPlayer = JsonPinnedPlayer(
        id = "",
        name = "",
        email = "",
        badge = "",
        callSignAdjective = "",
        callSignNoun = "",
        imageURL = "",
        avatarType = AvatarType.BoringBeam,
        pins = listOf(pinData),
        unvalidatedEmails = setOf(""),
    )
    val pinnedCouplingPair = JsonPinnedCouplingPair(
        players = notEmptyListOf(pinnedPlayer),
        pins = setOf(pinData),
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
    val integrationRecord = JsonIntegrationRecord(
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
    val contributionRecord = JsonContributionRecord(
        id = "",
        createdAt = Instant.DISTANT_FUTURE,
        dateTime = Instant.DISTANT_PAST,
        hash = "",
        ease = 99,
        story = "",
        link = "",
        participantEmails = setOf(""),
        partyId = PartyId(""),
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
    val partyRecord = JsonPartyDetailsRecord(
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

    val pinRecord = JsonPinRecord(
        id = "",
        name = "",
        icon = "",
        partyId = PartyId(""),
        modifyingUserEmail = "",
        isDeleted = false,
        timestamp = Instant.DISTANT_PAST,
    )

    val playerRecord = JsonPlayerRecord(
        id = "",
        name = "",
        email = "",
        badge = "",
        callSignAdjective = "",
        callSignNoun = "",
        imageURL = "",
        avatarType = "",
        partyId = PartyId(""),
        modifyingUserEmail = "",
        isDeleted = false,
        timestamp = Instant.DISTANT_PAST,
    )
}
