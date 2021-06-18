package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.dynamo.external.DynamoDBClient
import com.zegreatrob.coupling.dynamo.external.awsdynamoclient.DynamoDB
import com.zegreatrob.coupling.dynamo.external.awslibdynamodb.DynamoDBDocumentClient
import kotlin.js.Json
import kotlin.js.json

object DynamoDbProvider : DynamoDBSyntax {
    override val dynamoDB: DynamoDB by lazy {
        DynamoDB(dynamoConfig())
    }

    private fun dynamoConfig(): Json {
        val json = json("region" to "us-east-1")
        val secret = js("process.env.AWS_SECRET_ACCESS_KEY").unsafeCast<String?>()
        val localDynamo = js("process.env.LOCAL_DYNAMO").unsafeCast<String?>() == "true"
        return if (!localDynamo && secret != null) {
            json
        } else
            json.add(
                json(
                    "endpoint" to "http://localhost:8000",
                    "credentials" to json(
                        "accessKeyId" to "lol",
                        "secretAccessKey" to "lol"
                    )
                )
            )
    }

    override val dynamoDBClient: DynamoDBDocumentClient by lazy {
        DynamoDBDocumentClient.from(
            DynamoDBClient(
                json("convertEmptyValues" to true)
                    .add(dynamoConfig())
            )
        )
    }
}
