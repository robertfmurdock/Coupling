package com.zegreatrob.coupling

interface UserContext {
    val userEmail: String
    val tribeIds: List<String>
}

interface UserContextSyntax {
    val userContext: UserContext
    fun userEmail() = userContext.userEmail
}