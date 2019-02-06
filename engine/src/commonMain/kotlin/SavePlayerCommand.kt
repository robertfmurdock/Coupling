data class SavePlayerCommand(val tribeIdPlayer: TribeIdPlayer)

interface SavePlayerCommandDispatcher : TribeIdPlayerSaveSyntax {

    suspend fun SavePlayerCommand.perform() = tribeIdPlayer.save().let { tribeIdPlayer.player }

}
