interface TribeIdPlayersSyntax {
    val repository: PlayerRepository
    suspend fun TribeId.loadPlayers() = repository.getPlayersAsync(this).await()
}