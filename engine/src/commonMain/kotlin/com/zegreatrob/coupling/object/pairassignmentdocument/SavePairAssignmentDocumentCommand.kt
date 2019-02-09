data class SavePairAssignmentDocumentCommand(val tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument)

interface SavePairAssignmentDocumentCommandDispatcher : TribeIdPairAssignmentDocumentSaveSyntax {

    suspend fun SavePairAssignmentDocumentCommand.perform() = tribeIdPairAssignmentDocument.apply { save() }

}

interface TribeIdPairAssignmentDocumentSaveSyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentSaver

    suspend fun TribeIdPairAssignmentDocument.save() = pairAssignmentDocumentRepository.save(this)
}
