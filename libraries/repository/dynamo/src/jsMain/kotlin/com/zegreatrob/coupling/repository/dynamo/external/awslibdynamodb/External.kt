@file:JsModule("@aws-sdk/lib-dynamodb")

package com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb

import com.zegreatrob.coupling.repository.dynamo.external.DynamoDBClient
import kotlinx.js.JsPlainObject
import kotlin.js.Json
import kotlin.js.Promise

external object DynamoDBDocumentClient {
    fun from(also: DynamoDBClient): DynamoDBDocumentClient
    fun <T> send(command: dynamic, options: Json = definedExternally): Promise<T>
}

external class PutCommand(options: Json)
external class GetCommand(options: Json)

@JsName("GetCommand")
external class TypedGetCommand(input: GetItemCommandInput) {
    val input: GetItemCommandInput
}
external class ScanCommand(options: Json)
external class QueryCommand(options: Json)
external class BatchGetCommand(options: Json)
external class DeleteCommand(options: Json)
external class ExecuteStatementCommand(options: Json)
external class BatchExecuteStatementCommand(options: Json)
external class TransactWriteCommand(input: TransactWriteCommandInput) {
    val input: TransactWriteCommandInput
}

@JsPlainObject
sealed external interface DynamoKey {
    @JsName("PK")
    val partitionKey: String

    @JsName("SK")
    val sortKey: String
}

@JsPlainObject
sealed external interface GetItemCommandInput {
    @JsName("TableName")
    val tableName: String

    @JsName("Key")
    val key: DynamoKey

    @JsName("ConsistentRead")
    val consistentRead: Boolean
}

@JsPlainObject
sealed external interface TransactWriteCommandInput {
    @JsName("TransactItems")
    val transactItems: Array<TransactWriteItem>
}

@JsPlainObject
sealed external interface TransactWriteItem {
    @JsName("Put")
    val put: DynamoPutItemInput
}

@JsPlainObject
sealed external interface DynamoPutItemInput {
    @JsName("TableName")
    val tableName: String

    @JsName("Item")
    val item: Json

    @JsName("ConditionExpression")
    val conditionExpression: String

    @JsName("ExpressionAttributeValues")
    val expressionAttributeValues: Json?
}

sealed external interface GetCommandOutput {
    @JsName("Item")
    val item: Json?
}

sealed external interface TransactionCancellationReason {
    @JsName("Code")
    val code: String?
}

sealed external interface TransactionCanceledError {
    @JsName("name")
    val name: String?

    @JsName("CancellationReasons")
    val cancellationReasons: Array<TransactionCancellationReason>?
}
