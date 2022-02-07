package com.zegreatrob.coupling.dynamo

import com.zegreatrob.coupling.dynamo.external.awsdynamoclient.DynamoDB
import com.zegreatrob.coupling.dynamo.external.awslibdynamodb.DynamoDBDocumentClient

interface DynamoDBSyntax {
    val dynamoDB: DynamoDB

    val dynamoDBClient: DynamoDBDocumentClient
}