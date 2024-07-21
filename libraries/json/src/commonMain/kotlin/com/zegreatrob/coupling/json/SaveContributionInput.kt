package com.zegreatrob.coupling.json

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class SaveContributionInput(
    val partyId: String,
    val contributionId: String,
    val hash: String?,
    val dateTime: Instant?,
    val ease: Int?,
    val story: String?,
    val link: String?,
    val participantEmails: Set<String>,
    val semver: String?,
    val label: String?,
    val firstCommit: String?,
    val firstCommitDateTime: Instant?,
    val integrationDateTime: Instant?,
    val cycleTime: Duration?,
)
