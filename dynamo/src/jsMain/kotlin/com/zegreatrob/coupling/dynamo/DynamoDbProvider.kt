package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.dynamo.external.DocumentClient
import com.zegreatrob.coupling.dynamo.external.DynamoDB
import com.zegreatrob.coupling.dynamo.external.config
import kotlin.js.json

object DynamoDbProvider : DynamoDBSyntax {
    override val dynamoDB: DynamoDB by lazy {
        config.update(
            json(
                "region" to "us-east-1",
                "endpoint" to "http://localhost:8000"
            )
        )
        DynamoDB()
    }

    override val documentClient: DocumentClient by lazy {
        also { dynamoDB }.let { DocumentClient() }
    }
}