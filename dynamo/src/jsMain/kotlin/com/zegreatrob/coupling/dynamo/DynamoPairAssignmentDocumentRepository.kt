package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.document
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository


class DynamoPairAssignmentDocumentRepository private constructor(
    override val userEmail: String,
    override val clock: TimeProvider
) :
    PairAssignmentDocumentRepository, UserEmailSyntax, DynamoPairAssignmentDocumentJsonMapping {

    companion object : DynamoRepositoryCreatorSyntax<DynamoPairAssignmentDocumentRepository>,
        TribeCreateTableParamProvider,
        DynamoItemPutSyntax,
        DynamoItemDeleteSyntax,
        TribeIdDynamoItemListGetSyntax,
        DynamoDBSyntax by DynamoDbProvider {
        override val construct = ::DynamoPairAssignmentDocumentRepository
        override val tableName = "PAIR_ASSIGNMENTS"
    }

    override suspend fun save(tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument) = performPutItem(
        tribeIdPairAssignmentDocument.toDynamoJson()
    )

    override suspend fun getPairAssignmentRecords(tribeId: TribeId) = tribeId.scanForItemList()
        .map { it.toRecord(tribeId.with(it.toPairAssignmentDocument())) }
        .sortedByDescending { it.data.document.date }

    override suspend fun delete(tribeId: TribeId, pairAssignmentDocumentId: PairAssignmentDocumentId) =
        performDelete(pairAssignmentDocumentId.value, recordJson(now()), tribeId)

}
