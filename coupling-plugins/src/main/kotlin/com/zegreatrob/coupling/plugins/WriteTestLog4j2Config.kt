package com.zegreatrob.coupling.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class WriteTestLog4j2Config : DefaultTask() {

    @get:Input
    abstract val logFilePath: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun writeConfig() {
        val file = outputFile.get().asFile
        file.parentFile.mkdirs()
        file.writeText(buildConfig(logFilePath.get()))
    }

    companion object {
        fun buildConfig(@Suppress("UNUSED_PARAMETER") logFilePath: String): String {
            val dollar = '$'
            return """
                {
                  "Configuration": {
                    "Appenders": {
                      "Console": {
                        "JsonLayout": {
                          "keyValuePair": [{
                            "key": "timestamp",
                            "value": "$dollar$dollar{date:yyyy-MM-dd'T'HH:mm:ss.SSSZ}"
                          },{
                            "key": "testRunIdentifier",
                            "value": "$dollar$dollar{sys:testRunIdentifier}"
                          }],
                          "includeTimeMillis": true,
                          "eventEol": true,
                          "compact": true,
                          "objectMessageAsJsonObject": true
                        },
                        "name": "Console",
                        "target": "SYSTEM_OUT"
                      }
                    },
                    "Loggers": {
                      "Root": {
                        "AppenderRef": [
                          {
                            "ref": "Console"
                          }
                        ],
                        "level": "trace"
                      }
                    }
                  }
                }
            """.trimIndent()
        }
    }
}
