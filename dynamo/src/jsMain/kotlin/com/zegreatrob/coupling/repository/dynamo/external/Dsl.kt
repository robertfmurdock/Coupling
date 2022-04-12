package com.zegreatrob.coupling.repository.dynamo.external

import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.*
import kotlin.js.Json
import kotlin.js.Promise

fun DynamoDBDocumentClient.put(params: Json): Promise<Unit> = send(PutCommand(params))

fun DynamoDBDocumentClient.scan(params: Json): Promise<Json> = send(ScanCommand(params))

fun DynamoDBDocumentClient.query(params: Json): Promise<Json> = send(QueryCommand(params))

fun DynamoDBDocumentClient.batchGet(params: Json): Promise<Json> = send(BatchGetCommand(params))

fun DynamoDBDocumentClient.delete(params: Json): Promise<Json> = send(DeleteCommand(params))