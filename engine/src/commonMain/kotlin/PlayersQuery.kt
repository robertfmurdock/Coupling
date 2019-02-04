data class PlayersQuery(val tribeId: String)

interface PlayersQueryDispatcher : TribeIdPlayersSyntax {
    suspend fun PlayersQuery.perform() = tribeId.loadPlayers()
}
