package com.zegreatrob.coupling.action

sealed interface CommandResult :
    VoidResult {
    object Unauthorized : CommandResult
}

sealed interface VoidResult {
    object Accepted : VoidResult
    object Rejected : VoidResult
}
