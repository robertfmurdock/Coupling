package com.zegreatrob.coupling.model

import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import kotools.types.text.NotBlankString
import kotlin.time.Clock
import kotlin.time.Instant

data class Record<T>(
    val data: T,
    val modifyingUserId: NotBlankString?,
    val isDeleted: Boolean,
    val timestamp: Instant,
)

fun <T> List<Record<T>>.data() = map { it.data }

typealias PartyRecord<T> = Record<PartyElement<T>>

fun <T> partyRecord(
    partyId: PartyId,
    data: T,
    modifyingUserEmail: NotBlankString,
    isDeleted: Boolean = false,
    timestamp: Instant =
        Clock.System.now(),
) = PartyRecord(PartyElement(partyId, data), modifyingUserEmail, isDeleted, timestamp)

val <T> PartyRecord<T>.element get() = this.data.element

val <T> List<PartyRecord<T>>.elements get() = map { it.element }
