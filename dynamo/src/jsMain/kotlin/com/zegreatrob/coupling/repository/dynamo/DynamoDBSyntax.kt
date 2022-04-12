package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.repository.dynamo.external.awsdynamoclient.DynamoDB
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.DynamoDBDocumentClient

interface DynamoDBSyntax {
    val dynamoDB: DynamoDB

    val dynamoDBClient: DynamoDBDocumentClient
}