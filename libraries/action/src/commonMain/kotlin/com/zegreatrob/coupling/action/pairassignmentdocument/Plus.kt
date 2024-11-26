package com.zegreatrob.coupling.action.pairassignmentdocument

import kotools.types.collection.NotEmptyList
import kotools.types.collection.notEmptyListOf

inline fun <reified E> NotEmptyList<E>.plus(entry: E): NotEmptyList<E> = notEmptyListOf(head, tail = tail?.let { it.toList() + entry }?.toTypedArray() ?: arrayOf(entry))
