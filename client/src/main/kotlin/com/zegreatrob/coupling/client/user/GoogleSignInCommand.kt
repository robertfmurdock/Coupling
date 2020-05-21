package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.actionFunc.SimpleSuspendAction
import com.zegreatrob.coupling.actionFunc.successResult

object GoogleSignInCommand :
    SimpleSuspendAction<GoogleSignInCommandDispatcher, Unit> {
    override val performFunc = link(GoogleSignInCommandDispatcher::perform)
}

interface GoogleSignInCommandDispatcher : GoogleSignIn {
    suspend fun perform(action: GoogleSignInCommand) = signIn().successResult()
}
