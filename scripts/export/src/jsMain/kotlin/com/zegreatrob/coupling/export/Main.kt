package com.zegreatrob.coupling.export

import com.zegreatrob.coupling.model.user.User
import mu.KotlinLoggingConfiguration
import mu.KotlinLoggingLevel

val user = User("EXPORT_USER", "robert.f.murdock@gmail.com", emptySet())

fun main() {
    KotlinLoggingConfiguration.LOG_LEVEL = KotlinLoggingLevel.WARN
    exportWithDynamo()
//    exportWithMongo()
}
