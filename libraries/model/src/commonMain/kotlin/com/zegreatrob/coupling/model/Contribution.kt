package com.zegreatrob.coupling.model

import kotlinx.datetime.Instant

data class Contribution(
    val id: String,
    val createdAt: Instant,
    val dateTime: Instant?,
    val hash: String?,
    val ease: Int?,
    val story: String?,
    val link: String?,
    val participantEmails: Set<String>,
)
