interface TribeIdPlayersSyntax {
    val playerRepository: PlayerRepository
    suspend fun TribeId.loadPlayers() = playerRepository.getPlayersAsync(this).await()
}