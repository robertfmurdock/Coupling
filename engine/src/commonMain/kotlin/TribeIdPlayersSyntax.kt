interface TribeIdPlayersSyntax {
    val repository: PlayersRepository
    suspend fun TribeId.loadPlayers() = repository.getPlayersAsync(this).await()
}