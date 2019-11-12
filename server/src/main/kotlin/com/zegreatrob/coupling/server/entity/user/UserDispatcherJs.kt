package com.zegreatrob.coupling.server.entity.user

import com.zegreatrob.coupling.model.user.AuthenticatedUserEmailSyntax

interface UserDispatcherJs : AuthenticatedUserEmailSyntax, UserIsAuthorizedActionDispatcherJs,
    UserIsAuthorizedWithDataActionDispatcherJs
