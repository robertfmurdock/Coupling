data class DeletePairAssignmentDocumentCommand(val id: PairAssignmentDocumentId)

interface DeletePairAssignmentDocumentCommandDispatcher : PairAssignmentDocumentIdDeleteSyntax {

    suspend fun DeletePairAssignmentDocumentCommand.perform() = id.delete()

}

interface PairAssignmentDocumentIdDeleteSyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentDeleter

    suspend fun PairAssignmentDocumentId.delete() = pairAssignmentDocumentRepository.delete(this)
}
