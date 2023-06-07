package com.zegreatrob.coupling.action

sealed interface CommandResult {

    object Unauthorized : CommandResult
}
