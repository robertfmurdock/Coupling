interface TribeIdPlayersSyntax {
    val repository: CouplingDataRepository
    suspend fun String.loadPlayers() = repository.getPlayersAsync(this).await()
}