package com.zegreatrob.coupling.action

sealed interface CommandResult :
    VoidResult {
    data object Unauthorized : CommandResult
}

sealed interface VoidResult {
    data object Accepted : VoidResult
    data object Rejected : VoidResult
}
