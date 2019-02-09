data class RetiredPlayersQuery(val tribeId: TribeId)

interface RetiredPlayersQueryDispatcher : TribeIdRetiredPlayersSyntax {
    suspend fun RetiredPlayersQuery.perform() = tribeId.loadRetiredPlayers()
}
