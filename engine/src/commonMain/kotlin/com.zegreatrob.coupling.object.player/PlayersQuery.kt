data class PlayersQuery(val tribeId: TribeId)

interface PlayersQueryDispatcher : TribeIdPlayersSyntax {
    suspend fun PlayersQuery.perform() = tribeId.loadPlayers()
}
