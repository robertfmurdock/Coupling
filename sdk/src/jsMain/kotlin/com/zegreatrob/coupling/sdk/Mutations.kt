package com.zegreatrob.coupling.sdk

object Mutations {
    val spin = """
            mutation spin(${"\$input"}: SpinInput!) {
                spin(input: ${"\$input"}) {
                    result {
                        _id
                        date
                        pairs {
                            players {
                                _id
                                name
                                email
                                badge
                                callSignAdjective
                                callSignNoun
                                imageURL
                                pins {
                                    id
                                    icon
                                    name
                                }
                            }
                            pins {
                                id
                                icon
                                name
                            }
                        }
                    }
                }
            }
    """.trimIndent()

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