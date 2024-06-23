package com.zegreatrob.coupling.server.entity.user

import com.zegreatrob.coupling.model.user.CurrentUserIdProvider
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedAction

interface UserDispatcher :
    CurrentUserIdProvider,
    UserIsAuthorizedAction.Dispatcher
