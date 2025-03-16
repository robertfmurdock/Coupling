package com.zegreatrob.coupling.export

import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.model.user.UserId
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.github.oshai.kotlinlogging.Level
import kotools.types.text.toNotBlankString

val user = UserDetails(
    UserId("EXPORT_USER".toNotBlankString().getOrThrow()),
    "robert.f.murdock@gmail.com".toNotBlankString().getOrThrow(),
    emptySet(),
    null,
)

fun main() {
    KotlinLoggingConfiguration.logLevel = Level.WARN
    exportWithDynamo()
//    exportWithMongo()
}
