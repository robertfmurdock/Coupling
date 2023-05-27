package com.zegreatrob.coupling.server.entity.user

import com.zegreatrob.coupling.model.user.AuthenticatedUserEmailSyntax
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedAction

interface UserDispatcher : AuthenticatedUserEmailSyntax, UserIsAuthorizedAction.Dispatcher
