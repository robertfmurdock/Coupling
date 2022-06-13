package com.zegreatrob.coupling.repository.dynamo.external

import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.BatchGetCommand
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.DeleteCommand
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.DynamoDBDocumentClient
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.PutCommand
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.QueryCommand
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.ScanCommand
import kotlin.js.Json
import kotlin.js.Promise

fun DynamoDBDocumentClient.put(params: Json): Promise<Unit> = send(PutCommand(params))

fun DynamoDBDocumentClient.scan(params: Json): Promise<Json> = send(ScanCommand(params))

fun DynamoDBDocumentClient.query(params: Json): Promise<Json> = send(QueryCommand(params))

fun DynamoDBDocumentClient.batchGet(params: Json): Promise<Json> = send(BatchGetCommand(params))

fun DynamoDBDocumentClient.deleteIt(params: Json): Promise<Json> = send(DeleteCommand(params))
