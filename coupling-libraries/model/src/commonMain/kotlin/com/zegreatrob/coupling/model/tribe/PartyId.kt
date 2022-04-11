package com.zegreatrob.coupling.model.tribe

import kotlin.jvm.JvmInline

@JvmInline
value class PartyId(val value: String)

data class TribeElement<T>(val id: PartyId, val element: T)

fun <T> PartyId.with(element: T) = TribeElement(this, element)
fun <T> PartyId.with(elementList: List<T>) = elementList.map { this.with(it) }
