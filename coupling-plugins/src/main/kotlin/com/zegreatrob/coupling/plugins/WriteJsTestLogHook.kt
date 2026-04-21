package com.zegreatrob.coupling.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class WriteJsTestLogHook : DefaultTask() {

    @get:Input
    abstract val logFilePath: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun writeHook() {
        val file = outputFile.get().asFile
        file.parentFile.mkdirs()
        file.writeText(buildHookContents(logFilePath.get()))
    }

    companion object {
        fun buildHookContents(logFilePath: String): String {
            return """
                const fs = require('fs');
                const logPath = process.env.COUPLING_TEST_LOG_PATH || ${jsonEscape(logFilePath)};
                const runId = process.env.COUPLING_TEST_RUN_ID || 'unknown-run';
                const taskPath = process.env.COUPLING_TEST_TASK_PATH || '';
                if (logPath) {
                  const append = (args) => {
                    try {
                      const line = args.map(a => {
                        if (typeof a === 'string') return a;
                        try { return JSON.stringify(a); } catch (e) { return String(a); }
                      }).join(' ');
                      const event = {
                        type: 'Log',
                        platform: 'js',
                        run_id: runId,
                        task: taskPath,
                        logger: 'console',
                        message: line,
                        timestamp: new Date().toISOString()
                      };
                      fs.appendFileSync(logPath, JSON.stringify(event) + '\n');
                    } catch (e) {}
                  };
                  const origLog = console.log;
                  const origInfo = console.info;
                  const origWarn = console.warn;
                  const origError = console.error;
                  console.log = (...args) => { append(args); origLog.apply(console, args); };
                  console.info = (...args) => { append(args); origInfo.apply(console, args); };
                  console.warn = (...args) => { append(args); origWarn.apply(console, args); };
                  console.error = (...args) => { append(args); origError.apply(console, args); };
                }
            """.trimIndent()
        }

        private fun jsonEscape(value: String): String {
            val escaped = value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
            return "\"$escaped\""
        }
    }
}
