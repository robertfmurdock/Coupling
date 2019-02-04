interface TribeIdPlayersSyntax {
    val repository: PlayersRepository
    suspend fun String.loadPlayers() = repository.getPlayersAsync(this).await()
}