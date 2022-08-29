package com.zegreatrob.coupling.sdk

interface GraphQueries {
    val mutations: Mutations
    val queries: Queries
}

class Mutations(gqlFileLoader: GqlFileLoader) {
    val spin by gqlFileLoader
    val savePin by gqlFileLoader
    val saveBoost by gqlFileLoader
    val deleteBoost by gqlFileLoader
    val saveParty by gqlFileLoader
    val savePlayer by gqlFileLoader
    val savePairAssignments by gqlFileLoader
    val deleteParty by gqlFileLoader
    val deletePin by gqlFileLoader
    val deletePairAssignments by gqlFileLoader
    val deletePlayer by gqlFileLoader
}

class Queries(gqlFileLoader: GqlFileLoader) {
    val listParties by gqlFileLoader
    val user by gqlFileLoader
    val boost by gqlFileLoader
}
