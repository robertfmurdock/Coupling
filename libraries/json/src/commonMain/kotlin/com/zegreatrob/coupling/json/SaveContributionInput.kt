package com.zegreatrob.coupling.json

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

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
)
