package com.zegreatrob.coupling

import com.zegreatrob.coupling.common.entity.user.User

interface UserEmailSyntax {
    val userEmail: String
}

interface AuthenticatedUserSyntax {
    val user: User
}

interface AuthenticatedUserEmailSyntax : AuthenticatedUserSyntax, UserEmailSyntax {
    override val userEmail get() = user.email
}

interface UserAuthorizedTribeIdsSyntax : AuthenticatedUserSyntax {
    fun userAuthorizedTribeIds() = user.authorizedTribeIds
}