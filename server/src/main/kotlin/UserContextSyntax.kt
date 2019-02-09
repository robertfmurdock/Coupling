interface UserContextSyntax {
    val userContext: UserContext
    fun username() = userContext.username
}