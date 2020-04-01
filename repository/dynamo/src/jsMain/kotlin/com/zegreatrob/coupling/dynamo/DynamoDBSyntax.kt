package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.dynamo.external.DocumentClient
import com.zegreatrob.coupling.dynamo.external.DynamoDB

interface DynamoDBSyntax {
    val dynamoDB: DynamoDB

    val documentClient: DocumentClient
}