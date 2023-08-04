package com.zegreatrob.coupling.export

import com.zegreatrob.coupling.model.user.UserDetails
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.github.oshai.kotlinlogging.Level

val user = UserDetails("EXPORT_USER", "robert.f.murdock@gmail.com", emptySet(), null)

fun main() {
    KotlinLoggingConfiguration.LOG_LEVEL = Level.WARN
    exportWithDynamo()
//    exportWithMongo()
}
