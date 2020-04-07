package com.zegreatrob.coupling.model.user

interface UserEmailSyntax {
    val userId: String
}

interface AuthenticatedUserSyntax {
    val user: User
}

interface AuthenticatedUserEmailSyntax : AuthenticatedUserSyntax, UserEmailSyntax {
    override val userId get() = user.email
}

interface UserAuthorizedTribeIdsSyntax : AuthenticatedUserSyntax {
    fun userAuthorizedTribeIds() = user.authorizedTribeIds
}