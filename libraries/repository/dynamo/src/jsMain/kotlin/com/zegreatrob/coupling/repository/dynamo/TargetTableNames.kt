package com.zegreatrob.coupling.repository.dynamo

internal fun targetTableName(table: String, stage: String? = configuredStage()): String = when {
    stage == null || stage == "local" -> "${legacyDynamoPrefix()}$table"
    stage == "prod" -> table
    else -> "$stage-$table"
}

internal fun configuredStage(): String? = js("process.env.STAGE == null ? null : process.env.STAGE").unsafeCast<String?>()

internal fun legacyDynamoPrefix(): String = js("process.env.DYNAMO_PREFIX").unsafeCast<String?>()
    ?: error("No Dynamo Prefix Configured.")
