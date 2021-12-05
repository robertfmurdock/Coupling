package com.zegreatrob.coupling.sdk

object Mutations {
    val spin by LoadGqlFile
    val savePin by LoadGqlFile
    val saveTribe by LoadGqlFile
    val savePlayer by LoadGqlFile
    val savePairAssignments by LoadGqlFile
    val deleteTribe by LoadGqlFile
    val deletePin by LoadGqlFile
    val deletePairAssignments by LoadGqlFile
    val deletePlayer by LoadGqlFile
}

object Queries {
    val listTribes by LoadGqlFile
}