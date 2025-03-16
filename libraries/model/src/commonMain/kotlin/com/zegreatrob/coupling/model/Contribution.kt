package com.zegreatrob.coupling.model

import com.zegreatrob.coupling.model.party.PartyId
import kotlinx.datetime.Instant
import kotools.types.text.NotBlankString
import kotlin.time.Duration

data class Contribution(
    val id: NotBlankString,
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
    val commitCount: Int?,
    val name: String?,
)

data class ContributionInput(
    val contributionId: NotBlankString,
    val participantEmails: Set<String>,
    val hash: String? = null,
    val dateTime: Instant? = null,
    val ease: Int? = null,
    val story: String? = null,
    val link: String? = null,
    val semver: String? = null,
    val label: String? = null,
    val firstCommit: String? = null,
    val firstCommitDateTime: Instant? = null,
    val integrationDateTime: Instant? = null,
    val cycleTime: Duration? = null,
    val commitCount: Int? = null,
    val name: String? = null,
)

data class ContributionQueryParams(val partyId: PartyId, val window: Duration?, val limit: Int?)
