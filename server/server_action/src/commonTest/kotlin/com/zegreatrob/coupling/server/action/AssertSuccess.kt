package com.zegreatrob.coupling.server.action

import com.zegreatrob.testmints.async.Exercise
import kotlin.test.DefaultAsserter

fun <V> Result<V>.assertSuccess(successfulAssertions: V.() -> Unit = {}) = when (this) {
    is SuccessfulResult -> successfulAssertions(value)
    else -> DefaultAsserter.fail("Result $this was not successful")
}

infix fun <C : Any, R : Result<V>, V> Exercise<C, R>.verifySuccess(validations: C.(V) -> Unit) {
    verify { result -> result.assertSuccess { validations(this) } }
}