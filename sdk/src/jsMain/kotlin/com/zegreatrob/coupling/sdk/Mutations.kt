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

    val deleteTribe = """
        mutation deleteTribe(${"\$tribeId"}: String!) { 
            deleteTribe(tribeId: ${"\$tribeId"})
        }
    """.trimIndent()
}