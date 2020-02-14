package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository


class DynamoPairAssignmentDocumentRepository private constructor() : PairAssignmentDocumentRepository {

    companion object : DynamoRepositoryCreatorSyntax<DynamoPairAssignmentDocumentRepository>,
        TribeCreateTableParamProvider,
        DynamoItemPutSyntax,
        DynamoItemDeleteSyntax,
        TribeIdDynamoItemListGetSyntax,
        DynamoPairAssignmentDocumentJsonMapping,
        DynamoDBSyntax by DynamoDbProvider {
        override val construct = ::DynamoPairAssignmentDocumentRepository
        override val tableName = "PAIR_ASSIGNMENTS"
    }

    override suspend fun save(tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument) = performPutItem(
        tribeIdPairAssignmentDocument.toDynamoJson()
    )

    override suspend fun getPairAssignments(tribeId: TribeId): List<PairAssignmentDocument> = tribeId.scanForItemList()
        .map { it.toPairAssignmentDocument() }
        .sortedByDescending { it.date }

    override suspend fun delete(tribeId: TribeId, pairAssignmentDocumentId: PairAssignmentDocumentId) =
        performDelete(pairAssignmentDocumentId.value, tribeId)

}
