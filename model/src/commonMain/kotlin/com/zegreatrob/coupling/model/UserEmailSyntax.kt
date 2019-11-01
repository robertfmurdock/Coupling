package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.model.User

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