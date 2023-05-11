package com.zegreatrob.coupling.sdk

interface GraphQueries {
    val mutations: Mutation
    val queries: Query
}

object Mutation {
    val spin by GqlFileLoader
    val savePin by GqlFileLoader
    val saveBoost by GqlFileLoader
    val deleteBoost by GqlFileLoader
    val saveParty by GqlFileLoader
    val savePlayer by GqlFileLoader
    val savePairAssignments by GqlFileLoader
    val deleteParty by GqlFileLoader
    val deletePin by GqlFileLoader
    val deletePairAssignments by GqlFileLoader
    val deletePlayer by GqlFileLoader
}

object Query {
    val listParties by GqlFileLoader
    val user by GqlFileLoader
    val boost by GqlFileLoader
}
