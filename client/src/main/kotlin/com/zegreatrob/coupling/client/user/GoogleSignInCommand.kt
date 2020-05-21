package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult

object GoogleSignInCommand :
    SimpleSuspendResultAction<GoogleSignInCommandDispatcher, Unit> {
    override val performFunc = link(GoogleSignInCommandDispatcher::perform)
}

interface GoogleSignInCommandDispatcher : GoogleSignIn {
    suspend fun perform(action: GoogleSignInCommand) = signIn().successResult()
}
