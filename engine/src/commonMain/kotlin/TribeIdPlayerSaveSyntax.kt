interface TribeIdPlayerSaveSyntax {

    val repository: PlayerRepository

    suspend fun TribeIdPlayer.save() = repository.save(this)

}