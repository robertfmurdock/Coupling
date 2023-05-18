package com.zegreatrob.coupling.sdk.dsl

@CouplingQueryDsl
interface QueryBuilder<T> {
    val output: T
}
