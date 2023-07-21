package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.repository.dynamo.external.DynamoDBClient
import com.zegreatrob.coupling.repository.dynamo.external.awsdynamoclient.DynamoDB
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.DynamoDBDocumentClient
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.js.Json
import kotlin.js.json

object DynamoDbProvider : DynamoDBSyntax {
    override val dynamoDB: DynamoDB by lazy {
        DynamoDB(dynamoConfig())
    }

    private val logger = KotlinLogging.logger("DYNAMODB")

    private fun dynamoConfig(): Json {
        val json = json(
            "defaultsMode" to "standard",
            "region" to "us-east-1",
            "retryMode" to "standard",
            "useFipsEndpoint" to false,
            "useDualstackEndpoint" to false,
        )
        val secret = js("process.env.AWS_SECRET_ACCESS_KEY").unsafeCast<String?>()
        val localDynamo = js("process.env.LOCAL_DYNAMO").unsafeCast<String?>() == "true"
        return if (!localDynamo && secret != null) {
            json
        } else {
            json.add(
                json(
                    "endpoint" to nonAwsHost(),
                    "credentials" to json(
                        "accessKeyId" to "lol",
                        "secretAccessKey" to "lol",
                    ),
                ).let {
                    if (js("process.env.AWS_DYNAMO_LOGGING").unsafeCast<String?>() != "true") {
                        it
                    } else {
                        it.add(
                            json(
                                "logger" to json(
                                    "info" to { thing: dynamic -> logger.info { JSON.stringify(thing) } },
                                    "debug" to { thing: dynamic -> logger.debug { JSON.stringify(thing) } },
                                    "warn" to { thing: dynamic -> logger.warn { JSON.stringify(thing) } },
                                    "error" to { thing: dynamic -> logger.error { JSON.stringify(thing) } },
                                ),
                            ),
                        )
                    }
                },
            )
        }
    }

    private fun nonAwsHost() = js("process.env.LOCAL_DYNAMO_URL").unsafeCast<String?>()
        ?: "http://localhost:8000"

    override val dynamoDBClient: DynamoDBDocumentClient by lazy {
        DynamoDBDocumentClient.from(
            DynamoDBClient(
                json("convertEmptyValues" to true)
                    .add(dynamoConfig()),
            ),
        )
    }
}
