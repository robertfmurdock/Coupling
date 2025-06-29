package com.zegreatrob.coupling.repository.dynamo.slack

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.SlackTeamAccess
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.model.user.UserIdProvider
import com.zegreatrob.coupling.repository.dynamo.CreateTableParamProvider
import com.zegreatrob.coupling.repository.dynamo.DynamoDBSyntax
import com.zegreatrob.coupling.repository.dynamo.DynamoDbProvider
import com.zegreatrob.coupling.repository.dynamo.DynamoItemPutDeleteRecordSyntax
import com.zegreatrob.coupling.repository.dynamo.DynamoItemPutSyntax
import com.zegreatrob.coupling.repository.dynamo.DynamoQueryItemListGetSyntax
import com.zegreatrob.coupling.repository.dynamo.DynamoRecordJsonMapping
import com.zegreatrob.coupling.repository.dynamo.DynamoRepositoryCreatorSyntax
import com.zegreatrob.coupling.repository.dynamo.RecordSyntax
import com.zegreatrob.coupling.repository.slack.SlackAccessRepository
import kotlin.js.Json
import kotlin.js.json
import kotlin.time.Clock

class DynamoSlackRepository private constructor(override val userId: UserId, override val clock: Clock) :
    RecordSyntax,
    DynamoRecordJsonMapping,
    UserIdProvider,
    SlackAccessRepository {

    override suspend fun save(slackTeamAccess: SlackTeamAccess) = performPutItem(
        slackTeamAccess.toRecord().asDynamoJson(),
    )

    override suspend fun get(teamId: String): Record<SlackTeamAccess>? = performGetSingleItemQuery(teamId)
        ?.let { it.toRecord(it.toDomain()) }

    companion object :
        DynamoRepositoryCreatorSyntax<DynamoSlackRepository>(),
        CreateTableParamProvider,
        DynamoItemPutSyntax,
        DynamoQueryItemListGetSyntax,
        DynamoItemPutDeleteRecordSyntax,
        DynamoDBSyntax by DynamoDbProvider {
        override val construct = ::DynamoSlackRepository
        override val tableName = "SLACK"
    }

    private fun Record<SlackTeamAccess>.asDynamoJson() = recordJson()
        .add(
            json(
                "id" to data.teamId,
                "timestamp+id" to "${timestamp.isoWithMillis()}+${data.teamId}",
            ),
        )
        .add(data.toDynamoJson())

    private fun SlackTeamAccess.toDynamoJson() = nullFreeJson(
        "id" to teamId,
        "accessToken" to accessToken,
        "appId" to appId,
        "slackUserId" to slackUserId,
        "slackBotUserId" to slackBotUserId,
    )
}

private fun Json.toDomain(): SlackTeamAccess = SlackTeamAccess(
    teamId = this["id"].toString(),
    accessToken = this["accessToken"].toString(),
    appId = this["appId"].toString(),
    slackUserId = this["slackUserId"].toString(),
    slackBotUserId = this["slackBotUserId"].toString(),
)
