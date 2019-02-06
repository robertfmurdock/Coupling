interface TribeIdPlayerSaveSyntax {

    val repository: PlayersRepository

    suspend fun TribeIdPlayer.save() = repository.save(this)

}