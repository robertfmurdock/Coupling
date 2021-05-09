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
                                    _id
                                    icon
                                    name
                                }
                            }
                            pins {
                                _id
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

    val deletePlayer = """
        mutation deletePlayer(${"\$input"}: DeletePlayerInput!) {
            deletePlayer(input: ${"\$input"})
        }
    """.trimIndent()
}