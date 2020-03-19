package com.zegreatrob.coupling.model.tribe

inline class TribeId(val value: String)

data class TribeElement<T>(val id: TribeId, val element: T)

fun <T> TribeId.with(element: T) = TribeElement(this, element)
fun <T> TribeId.with(elementList: List<T>) = elementList.map { this.with(it) }
