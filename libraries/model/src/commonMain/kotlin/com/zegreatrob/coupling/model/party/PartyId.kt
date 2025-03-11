package com.zegreatrob.coupling.model.party

import kotools.types.text.NotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi

data class PartyId(val value: NotBlankString)

@OptIn(ExperimentalKotoolsTypesApi::class)
fun PartyId(value: String) = PartyId(NotBlankString.create(value))

data class PartyElement<T>(val partyId: PartyId, val element: T)

fun <T> PartyId.with(element: T) = PartyElement(this, element)
fun <T> PartyId.with(elementList: List<T>) = elementList.map { this.with(it) }
