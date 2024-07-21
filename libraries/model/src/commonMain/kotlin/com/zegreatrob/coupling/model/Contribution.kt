package com.zegreatrob.coupling.model

import com.zegreatrob.coupling.model.party.PartyId
import kotlinx.datetime.Instant
import kotlin.time.Duration

data class Contribution(
    val id: String,
    val createdAt: Instant,
    val dateTime: Instant?,
    val hash: String?,
    val firstCommit: String?,
    val firstCommitDateTime: Instant?,
    val ease: Int?,
    val story: String?,
    val link: String?,
    val participantEmails: Set<String>,
    val label: String?,
    val semver: String?,
    val integrationDateTime: Instant?,
    val cycleTime: Duration?,
)

data class ContributionQueryParams(val partyId: PartyId, val window: Duration?, val limit: Int?)
