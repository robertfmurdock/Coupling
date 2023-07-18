package com.zegreatrob.coupling.model

import kotools.types.collection.NotEmptyList

inline fun <I, reified O> NotEmptyList<I>.flatMap(crossinline notEmptyProducer: (I) -> NotEmptyList<O>): List<O> =
    toList()
        .flatMap { notEmptyProducer(it).toList() }
