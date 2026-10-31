package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.repository.discord.DiscordOAuthRepository
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.DynamoDeleteItemInput
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.DynamoPutItemInput
import com.zegreatrob.coupling.repository.dynamo.external.deleteIt
import com.zegreatrob.coupling.repository.dynamo.external.isConditionalCheckFailed
import com.zegreatrob.coupling.repository.dynamo.external.put
import kotlinx.coroutines.await
import kotlin.js.json
import kotlin.time.Clock

class DynamoDiscordOAuthRepository(private val clock: Clock) :
    DiscordOAuthRepository,
    DynamoDBSyntax by DynamoDbProvider,
    DynamoTableNameSyntax {
    override val tableName = "DISCORD"

    override suspend fun savePending(stateHash: String, userId: UserId, partyId: PartyId, challenge: String, expiresAt: Long) {
        dynamoDBClient.put(
            DynamoPutItemInput(
                tableName = prefixedTableName,
                item = key(stateHash).add(json("userId" to userId.value.toString(), "partyId" to partyId.value.toString(), "challenge" to challenge, "expiresAt" to expiresAt.toDouble())),
                conditionExpression = "attribute_not_exists(tribeId)",
            ),
        ).await()
    }

    override suspend fun consume(stateHash: String, userId: UserId, partyId: PartyId, challenge: String): Boolean = try {
        dynamoDBClient.deleteIt(
            DynamoDeleteItemInput(
                tableName = prefixedTableName,
                key = key(stateHash),
                conditionExpression = "userId = :user AND partyId = :party AND challenge = :challenge AND expiresAt > :now",
                expressionAttributeValues = json(":user" to userId.value.toString(), ":party" to partyId.value.toString(), ":challenge" to challenge, ":now" to clock.now().toEpochMilliseconds().toDouble()),
            ),
        ).await()
        true
    } catch (error: Throwable) {
        if (!error.isConditionalCheckFailed()) throw error
        deleteExpired(stateHash)
        false
    }

    private suspend fun deleteExpired(stateHash: String) {
        try {
            dynamoDBClient.deleteIt(
                DynamoDeleteItemInput(
                    tableName = prefixedTableName,
                    key = key(stateHash),
                    conditionExpression = "expiresAt <= :now",
                    expressionAttributeValues = json(":now" to clock.now().toEpochMilliseconds().toDouble()),
                ),
            ).await()
        } catch (error: Throwable) {
            if (!error.isConditionalCheckFailed()) throw error
        }
    }

    private fun key(stateHash: String) = json("tribeId" to "OAUTH#DISCORD#$stateHash", "timestamp+id" to "PENDING")
}
