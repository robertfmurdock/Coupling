import kotlinx.coroutines.Deferred

interface CouplingDataRepository : PlayersRepository {
    fun getPinsAsync(tribeId: String): Deferred<List<Pin>>
    fun getHistoryAsync(tribeId: String): Deferred<List<PairAssignmentDocument>>
    fun getTribeAsync(tribeId: String): Deferred<KtTribe>
}

interface PlayersRepository {
    fun getPlayersAsync(tribeId: TribeId) : Deferred<List<Player>>
    suspend fun save(player: Player, tribeId: TribeId)
    suspend fun delete(playerId: String)
}
