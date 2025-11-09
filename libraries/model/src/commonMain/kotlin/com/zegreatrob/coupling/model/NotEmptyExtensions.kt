package com.zegreatrob.coupling.model

import kotools.types.collection.NotEmptyList
import kotools.types.collection.notEmptyListOf

inline fun <I, reified O> NotEmptyList<I>.map(function: (I) -> O): NotEmptyList<O> = notEmptyListOf(
    head = function(head),
    tail = tail?.toList()?.map(function)?.toTypedArray<O>() ?: emptyArray<O>(),
)

inline fun <I, reified O> NotEmptyList<I>.flatMap(crossinline function: (I) -> Iterable<O>): List<O> = toList()
    .flatMap(function)

operator fun <E> NotEmptyList<E>.get(index: Int): E = toList()[index]

inline fun <E> NotEmptyList<E>.forEach(function: (e: E) -> Unit) = toList().forEach(function)
