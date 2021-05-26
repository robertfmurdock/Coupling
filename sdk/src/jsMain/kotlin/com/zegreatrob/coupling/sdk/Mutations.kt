package com.zegreatrob.coupling.sdk

import kotlin.js.Json

object Mutations {
    val spin = loadTextFile("spin")

    private fun loadTextFile(path: String) = kotlinext.js.require("fs")
        ?.readFileSync.unsafeCast<((String, String) -> dynamic)?>()
        ?.let { readFileSync ->
            js("process.env").unsafeCast<Json>()["NODE_PATH"].unsafeCast<String>()
                .split(":")
                .asSequence()
                .mapNotNull { nodePath ->
                    try {
                        readFileSync("$nodePath/$path.graphql", "utf8").unsafeCast<String?>()
                    } catch (any: Throwable) {
                        null
                    }
                }
                .first()
        }
        ?: kotlinext.js.require("/$path.graphql").default.unsafeCast<String>()

    val savePin = """
        mutation savePin(${"\$input"}: SavePinInput!) {
            savePin(input: ${"\$input"})
        }
    """.trimIndent()

    val saveTribe = """
        mutation saveTribe(${"\$input"}: SaveTribeInput!) {
            saveTribe(input: ${"\$input"})
        }
    """.trimIndent()

    val savePlayer = """
        mutation savePlayer(${"\$input"}: SavePlayerInput!) {
            savePlayer(input: ${"\$input"})
        }
    """.trimIndent()

    val savePairAssignments = """
        mutation savePairAssignments(${"\$input"}: SavePairAssignmentsInput!) {
            savePairAssignments(input: ${"\$input"})
        }
    """.trimIndent()

    val deleteTribe = """
        mutation deleteTribe(${"\$input"}: DeleteTribeInput!) { 
            deleteTribe(input: ${"\$input"})
        }
    """.trimIndent()

    val deletePin = """
        mutation deletePin(${"\$input"}: DeletePinInput!) {
            deletePin(input: ${"\$input"})
        }
    """.trimIndent()

    val deletePairAssignments = """
        mutation deletePairAssignments(${"\$input"}: DeletePairAssignmentsInput!) {
            deletePairAssignments(input: ${"\$input"})
        }
    """.trimIndent()

    val deletePlayer = """
        mutation deletePlayer(${"\$input"}: DeletePlayerInput!) {
            deletePlayer(input: ${"\$input"})
        }
    """.trimIndent()
}