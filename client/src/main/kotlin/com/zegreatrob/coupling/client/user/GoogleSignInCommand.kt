package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.actionFunc.SimpleSuspendResultAction
import com.zegreatrob.coupling.actionFunc.successResult

object GoogleSignInCommand :
    SimpleSuspendResultAction<GoogleSignInCommandDispatcher, Unit> {
    override val performFunc = link(GoogleSignInCommandDispatcher::perform)
}

interface GoogleSignInCommandDispatcher : GoogleSignIn {
    suspend fun perform(action: GoogleSignInCommand) = signIn().successResult()
}
