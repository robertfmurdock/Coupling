data class PlayersQuery(val tribeId: String)

interface PlayersQueryDispatcher {

    val repository: CouplingDataRepository

    suspend fun PlayersQuery.perform(): List<Player> = repository.getPlayersAsync(tribeId).await()

}