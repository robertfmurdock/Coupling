package com.zegreatrob.coupling.client.user

import com.zegreatrob.testmints.action.async.SimpleSuspendAction

object GoogleSignInCommand : SimpleSuspendAction<GoogleSignInCommandDispatcher, Unit> {
    override val performFunc = link(GoogleSignInCommandDispatcher::perform)
}

interface GoogleSignInCommandDispatcher : GoogleSignIn {
    suspend fun perform(action: GoogleSignInCommand) = signIn()
}
