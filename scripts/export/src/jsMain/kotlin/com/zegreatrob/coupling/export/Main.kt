package com.zegreatrob.coupling.export

import com.zegreatrob.coupling.model.user.User
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.github.oshai.kotlinlogging.Level

val user = User("EXPORT_USER", "robert.f.murdock@gmail.com", emptySet(), null)

fun main() {
    KotlinLoggingConfiguration.LOG_LEVEL = Level.WARN
    exportWithDynamo()
//    exportWithMongo()
}
