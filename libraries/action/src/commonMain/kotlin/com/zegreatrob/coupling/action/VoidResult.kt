package com.zegreatrob.coupling.action

fun Boolean.voidResult() = when (this) {
    true -> VoidResult.Accepted
    false -> VoidResult.Rejected
}
