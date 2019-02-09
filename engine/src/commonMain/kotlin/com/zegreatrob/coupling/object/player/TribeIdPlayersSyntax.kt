interface TribeIdPlayersSyntax {
    val playerRepository: PlayerGetter
    suspend fun TribeId.loadPlayers() = playerRepository.getPlayersAsync(this).await()
}

interface TribeIdRetiredPlayersSyntax {
    val playerRepository: PlayerGetter
    suspend fun TribeId.loadRetiredPlayers() = playerRepository.getDeletedAsync(this).await()
}