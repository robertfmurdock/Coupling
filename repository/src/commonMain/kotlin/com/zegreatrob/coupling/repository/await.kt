package com.zegreatrob.coupling.repository

import kotlinx.coroutines.Deferred

suspend fun <T1, T2> await(d1: Deferred<T1>, d2: Deferred<T2>) = Pair(
    d1.await(),
    d2.await()
)

suspend fun <T1, T2, T3> await(d1: Deferred<T1>, d2: Deferred<T2>, d3: Deferred<T3>) = Triple(
    d1.await(),
    d2.await(),
    d3.await()
)
