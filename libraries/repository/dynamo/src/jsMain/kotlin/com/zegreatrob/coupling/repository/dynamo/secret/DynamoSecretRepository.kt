package com.zegreatrob.coupling.repository.dynamo.secret

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.model.party.SecretId
import com.zegreatrob.coupling.model.party.SecretUsed
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.model.user.UserIdProvider
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
import com.zegreatrob.coupling.repository.secret.SecretRepository
import kotlinx.datetime.Clock

class DynamoSecretRepository private constructor(override val userId: UserId, override val clock: Clock) :
    DynamoSecretJsonMapping,
    SecretRepository,
    SecretDelete,
    RecordSyntax,
    UserIdProvider {

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

    override suspend fun save(used: SecretUsed) = getSecrets(used.partyId).elements
        .firstOrNull { it.id == used.secretId }
        ?.copy(lastUsedTimestamp = used.lastUsedTimestamp)
        ?.let { used.partyId.with(it) }
        ?.let { save(it) }
        .let { }

    override suspend fun getSecrets(partyId: PartyId): List<PartyRecord<Secret>> = partyId.queryForItemList()
        .mapNotNull { it.toRecord() }

    override suspend fun deleteSecret(partyId: PartyId, secretId: SecretId) = performDelete(
        secretId.value.toString(),
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
