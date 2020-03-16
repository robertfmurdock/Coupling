package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.dynamo.external.DocumentClient
import com.zegreatrob.coupling.dynamo.external.DynamoDB
import com.zegreatrob.coupling.dynamo.external.config
import kotlin.js.Json
import kotlin.js.json

object DynamoDbProvider : DynamoDBSyntax {
    override val dynamoDB: DynamoDB by lazy {
        config.update(dynamoConfig())
        DynamoDB()
    }

    private fun dynamoConfig(): Json {
        val secret = js("process.env.AWS_SECRET_ACCESS_KEY")
        val json = json("region" to "us-east-1")
        return if (secret == null)
            json.add(json("endpoint" to "http://localhost:8000"))
        else {
            json
        }
    }

    override val documentClient: DocumentClient by lazy {
        also { dynamoDB }.let { DocumentClient() }
    }
}