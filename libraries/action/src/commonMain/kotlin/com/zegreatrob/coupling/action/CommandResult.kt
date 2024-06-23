package com.zegreatrob.coupling.action

sealed interface CommandResult :
    SpinCommand.Result,
    SubscriptionCommandResult,
    VoidResult {
    data object Unauthorized : CommandResult
}

sealed interface SubscriptionCommandResult : ApplyBoostCommand.Result {
    data object SubscriptionNotActive : ApplyBoostCommand.Result
}

sealed interface VoidResult {
    data object Accepted : VoidResult
    data object Rejected : VoidResult
}
