package com.zegreatrob.coupling.model.party

import kotlin.jvm.JvmInline

@JvmInline
value class PartyId(val value: String)

data class PartyElement<T>(val id: PartyId, val element: T)

fun <T> PartyId.with(element: T) = PartyElement(this, element)
fun <T> PartyId.with(elementList: List<T>) = elementList.map { this.with(it) }
