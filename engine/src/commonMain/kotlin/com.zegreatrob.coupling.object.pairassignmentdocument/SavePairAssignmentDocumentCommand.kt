data class SavePairAssignmentDocumentCommand(val pairAssignmentDocument: PairAssignmentDocument)

interface SavePairAssignmentDocumentCommandDispatcher : TribeIdPairAssignmentDocumentSaveSyntax {

    suspend fun SavePairAssignmentDocumentCommand.perform() = pairAssignmentDocument.apply { save() }

}

interface TribeIdPairAssignmentDocumentSaveSyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentSaver

    suspend fun PairAssignmentDocument.save() = pairAssignmentDocumentRepository.save(this)
}
