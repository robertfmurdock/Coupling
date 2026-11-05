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

@JsName("PutCommand")
external class TypedPutCommand(input: DynamoPutItemInput)
external class GetCommand(options: Json)

@JsName("GetCommand")
external class TypedGetCommand(input: GetItemCommandInput) {
    val input: GetItemCommandInput
}
external class ScanCommand(options: Json)
external class QueryCommand(options: Json)

@JsName("QueryCommand")
external class TypedQueryCommand(val input: DynamoQueryInput)
external class BatchGetCommand(options: Json)
external class DeleteCommand(options: Json)

@JsName("DeleteCommand")
external class TypedDeleteCommand(input: DynamoDeleteItemInput)
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
    val put: DynamoPutItemInput?

    @JsName("Delete")
    val delete: DynamoDeleteItemInput?

    @JsName("ConditionCheck")
    val conditionCheck: DynamoConditionCheck?
}

@JsPlainObject
sealed external interface DynamoPutItemInput {
    @JsName("TableName")
    val tableName: String

    @JsName("Item")
    val item: Json

    @JsName("ConditionExpression")
    val conditionExpression: String?

    @JsName("ExpressionAttributeValues")
    val expressionAttributeValues: Json?
}

@JsPlainObject
sealed external interface DynamoDeleteItemInput {
    @JsName("TableName")
    val tableName: String

    @JsName("Key")
    val key: Json

    @JsName("ConditionExpression")
    val conditionExpression: String?

    @JsName("ExpressionAttributeValues")
    val expressionAttributeValues: Json?
}

sealed external interface DynamoDBServiceError {
    val name: String?
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

@JsPlainObject
sealed external interface DynamoQueryInput {
    @JsName("TableName")
    val tableName: String

    @JsName("KeyConditionExpression")
    val keyConditionExpression: String

    @JsName("ExpressionAttributeNames")
    val expressionAttributeNames: Json

    @JsName("ExpressionAttributeValues")
    val expressionAttributeValues: Json

    @JsName("ConsistentRead")
    val consistentRead: Boolean

    @JsName("ScanIndexForward")
    val scanIndexForward: Boolean

    @JsName("Limit")
    val limit: Int

    @JsName("ExclusiveStartKey")
    val exclusiveStartKey: Json?
}

sealed external interface DynamoQueryOutput {
    @JsName("Items")
    val items: Array<Json>

    @JsName("LastEvaluatedKey")
    val lastEvaluatedKey: Json?
}

@JsPlainObject
sealed external interface DynamoConditionCheck {
    @JsName("TableName")
    val tableName: String

    @JsName("Key")
    val key: DynamoKey

    @JsName("ConditionExpression")
    val conditionExpression: String

    @JsName("ExpressionAttributeValues")
    val expressionAttributeValues: Json
}
