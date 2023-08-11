package com.zegreatrob.coupling.action

sealed interface CommandResult :
    SpinCommand.Result,
    SubscriptionCommandResult,
    VoidResult {
    data object Unauthorized : CommandResult
}

sealed interface SubscriptionCommandResult :
    SaveBoostCommand.Result {
    data object SubscriptionNotActive : SaveBoostCommand.Result
}

sealed interface VoidResult {
    data object Accepted : VoidResult
    data object Rejected : VoidResult
}
