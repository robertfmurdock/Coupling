package com.zegreatrob.coupling.repository.dynamo.secret

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.user.UserIdSyntax
import com.zegreatrob.coupling.repository.dynamo.DynamoDBSyntax
import com.zegreatrob.coupling.repository.dynamo.DynamoDbProvider
import com.zegreatrob.coupling.repository.dynamo.DynamoItemPutDeleteRecordSyntax
import com.zegreatrob.coupling.repository.dynamo.DynamoItemPutSyntax
import com.zegreatrob.coupling.repository.dynamo.DynamoRepositoryCreatorSyntax
import com.zegreatrob.coupling.repository.dynamo.DynamoSecretJsonMapping
import com.zegreatrob.coupling.repository.dynamo.PartyCreateTableParamProvider
import com.zegreatrob.coupling.repository.dynamo.PartyIdDynamoItemListGetSyntax
import com.zegreatrob.coupling.repository.dynamo.RecordSyntax
import com.zegreatrob.coupling.repository.secret.SecretDelete
import com.zegreatrob.coupling.repository.secret.SecretListGet
import com.zegreatrob.coupling.repository.secret.SecretSave
import korlibs.time.TimeProvider

class DynamoSecretRepository private constructor(override val userId: String, override val clock: TimeProvider) :
    DynamoSecretJsonMapping,
    SecretSave,
    SecretDelete,
    SecretListGet,
    RecordSyntax,
    UserIdSyntax {

    companion object :
        DynamoRepositoryCreatorSyntax<DynamoSecretRepository>(),
        PartyCreateTableParamProvider,
        DynamoItemPutSyntax,
        PartyIdDynamoItemListGetSyntax,
        DynamoItemPutDeleteRecordSyntax,
        DynamoDBSyntax by DynamoDbProvider {
        override val construct = ::DynamoSecretRepository
        override val tableName = "SECRET"
    }

    override suspend fun save(it: PartyElement<Secret>) = performPutItem(
        it.toRecord().asDynamoJson(),
    )

    override suspend fun getSecrets(partyId: PartyId): List<PartyRecord<Secret>> = partyId.queryForItemList()
        .map { it.toRecord() }

    override suspend fun deleteSecret(partyId: PartyId, secretId: String) = performDelete(
        secretId,
        partyId,
        now(),
        { toRecord() },
        { asDynamoJson() },
    )

    suspend fun getSecretRecords(partyId: PartyId) = partyId.logAsync("itemList") {
        queryAllRecords(partyId.itemListQueryParams())
    }.map {
        it.toRecord(partyId.with(it.toSecret()))
    }

    suspend fun saveRawRecord(record: Record<PartyElement<Secret>>) = performPutItem(
        record.asDynamoJson(),
    )
}
