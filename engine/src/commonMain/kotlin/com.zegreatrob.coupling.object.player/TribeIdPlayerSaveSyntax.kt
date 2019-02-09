interface TribeIdPlayerSaveSyntax {

    val playerRepository: PlayerRepository

    suspend fun TribeIdPlayer.save() = playerRepository.save(this)

}