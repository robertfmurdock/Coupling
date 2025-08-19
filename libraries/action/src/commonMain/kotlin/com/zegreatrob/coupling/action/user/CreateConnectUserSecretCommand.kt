package com.zegreatrob.coupling.action.user

import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
object CreateConnectUserSecretCommand {
    fun interface Dispatcher {
        suspend fun perform(command: CreateConnectUserSecretCommand): Pair<Secret, String>?
    }
}
