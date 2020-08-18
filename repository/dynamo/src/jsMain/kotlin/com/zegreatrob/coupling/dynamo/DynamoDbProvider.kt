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
        val json = json("region" to "us-east-1")
        val secret = js("process.env.AWS_SECRET_ACCESS_KEY").unsafeCast<String?>()
        val localDynamo = js("process.env.LOCAL_DYNAMO").unsafeCast<String?>() == "true"
        return if (!localDynamo && secret != null) {
            json
        } else
            json.add(json("endpoint" to "http://localhost:8000"))
    }

    override val documentClient: DocumentClient by lazy {
        DocumentClient(json("convertEmptyValues" to true)).also { dynamoDB }
    }
}
