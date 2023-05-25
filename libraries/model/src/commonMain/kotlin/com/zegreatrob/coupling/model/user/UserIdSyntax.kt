package com.zegreatrob.coupling.model.user

interface UserIdSyntax {
    val userId: String
}

interface AuthenticatedUserSyntax {
    val user: User
}

interface AuthenticatedUserEmailSyntax : AuthenticatedUserSyntax, UserIdSyntax {
    override val userId get() = user.email
}

interface UserAuthorizedPartyIdsSyntax : AuthenticatedUserSyntax {
    fun userAuthorizedPartyIds() = user.authorizedPartyIds
}
