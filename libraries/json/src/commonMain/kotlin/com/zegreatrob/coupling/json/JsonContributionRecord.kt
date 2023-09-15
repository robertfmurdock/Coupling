@file:UseSerializers(DateTimeSerializer::class, PartyIdSerializer::class)

package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class JsonContributionRecord(
    val id: String,
    val createdAt: Instant,
    val dateTime: Instant?,
    val hash: String?,
    val ease: Int?,
    val story: String?,
    val link: String?,
    val label: String?,
    val firstCommit: String?,
    val semver: String?,
    val participantEmails: Set<String>,
    override val partyId: PartyId?,
    override val modifyingUserEmail: String,
    override val isDeleted: Boolean,
    override val timestamp: Instant,
) : JsonPartyRecordInfo

fun PartyRecord<Contribution>.toJson() = JsonContributionRecord(
    id = data.element.id,
    createdAt = data.element.createdAt,
    dateTime = data.element.dateTime,
    hash = data.element.hash,
    ease = data.element.ease,
    story = data.element.story,
    link = data.element.link,
    semver = data.element.semver,
    label = data.element.label,
    firstCommit = data.element.firstCommit,
    participantEmails = data.element.participantEmails,
    partyId = data.partyId,
    modifyingUserEmail = modifyingUserId,
    isDeleted = isDeleted,
    timestamp = timestamp,
)

fun JsonContributionRecord.toModel() = PartyRecord(
    data = PartyId(id).with(
        Contribution(
            id = id,
            createdAt = createdAt,
            dateTime = dateTime,
            hash = hash,
            ease = ease,
            story = story,
            link = link,
            participantEmails = participantEmails,
            label = label,
            semver = semver,
            firstCommit = firstCommit,
        ),
    ),
    modifyingUserId = modifyingUserEmail,
    isDeleted = isDeleted,
    timestamp = timestamp,
)
