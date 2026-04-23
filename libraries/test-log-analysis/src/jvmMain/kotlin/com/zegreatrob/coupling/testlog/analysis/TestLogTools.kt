package com.zegreatrob.coupling.testlog.analysis

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File

enum class TestLogCommand {
    VALIDATE,
    ANALYZE,
}

data class TestLogRequest(
    val command: TestLogCommand,
    val args: List<String>,
)

data class TestLogRunResult(
    val exitCode: Int,
    val outputJson: String? = null,
    val errorOutput: String? = null,
)

object TestLogTools {
    private val mapper = ObjectMapper()
    private const val DEFAULT_LOG_PATH = "build/test-output/test.jsonl"
    private const val DEFAULT_MAX_OFFENDERS = 20
    private val requiredByAny = listOf("type", "timestamp", "run_id", "platform")
    private val requiredByEnd = listOf("status", "duration_ms", "task", "suite", "test")
    private val endEventTypes = setOf("TestEnd", "StepEnd")

    fun run(request: TestLogRequest): TestLogRunResult = when (request.command) {
        TestLogCommand.VALIDATE -> runValidate(request.args)

        TestLogCommand.ANALYZE -> TestLogRunResult(
            exitCode = 0,
            outputJson = """{"mode":"stub-analyze","status":"ok"}""",
        )
    }

    private fun runValidate(args: List<String>): TestLogRunResult {
        val options = parseValidateOptions(args)

        val file = File(options.filePath)
        if (!file.exists()) {
            return TestLogRunResult(
                exitCode = 2,
                errorOutput = "ERROR: file not found: ${options.filePath}",
            )
        }

        val lines = splitJsonlLikeNode(file.readText())
        val counts = ValidateCounts(file = options.filePath, totalLines = lines.size)
        val offenders = mutableListOf<Offender>()

        lines.forEachIndexed { index, line ->
            if (line.isBlank()) {
                return@forEachIndexed
            }

            counts.nonEmptyLines += 1

            val parsed = try {
                mapper.readTree(line).also {
                    counts.parsedJsonLines += 1
                }
            } catch (_: Exception) {
                counts.nonJsonLines += 1
                addOffender(offenders, options.maxOffenders, index + 1, "non-json", line)
                return@forEachIndexed
            }

            addCount(counts.typeCounts, parsed.get("type"))
            addCount(counts.platformCounts, parsed.get("platform"))

            val missingCore = missingKeys(parsed, requiredByAny)
            if (missingCore.isNotEmpty()) {
                counts.missingCoreFields += 1
                addOffender(offenders, options.maxOffenders, index + 1, "missing core: ${missingCore.joinToString(",")}", line)
            }

            val type = parsed.get("type")?.takeUnless { it.isNull }?.asText()
            if (type in endEventTypes) {
                val missingEnd = missingKeys(parsed, requiredByEnd)
                if (missingEnd.isNotEmpty()) {
                    counts.missingEndFields += 1
                    addOffender(offenders, options.maxOffenders, index + 1, "missing end: ${missingEnd.joinToString(",")}", line)
                }
                if (parsed.has("duration_ms") && !parsed.get("duration_ms").isNumber) {
                    counts.badDurationMs += 1
                    addOffender(offenders, options.maxOffenders, index + 1, "duration_ms not numeric", line)
                }
            }
        }

        val totalViolations =
            counts.nonJsonLines +
                counts.missingCoreFields +
                counts.missingEndFields +
                counts.badDurationMs

        val failingViolations = if (options.strictMode) {
            totalViolations
        } else {
            (if (options.failOnNonJson) counts.nonJsonLines else 0) +
                (if (options.failOnMissingCore) counts.missingCoreFields else 0)
        }

        val mode = if (options.strictMode) {
            "strict"
        } else if (options.failOnNonJson && options.failOnMissingCore) {
            "compat-fail-non-json-core"
        } else if (options.failOnNonJson) {
            "compat-fail-non-json"
        } else if (options.failOnMissingCore) {
            "compat-fail-core"
        } else {
            "compat"
        }

        val report = linkedMapOf<String, Any>(
            "file" to counts.file,
            "total_lines" to counts.totalLines,
            "non_empty_lines" to counts.nonEmptyLines,
            "parsed_json_lines" to counts.parsedJsonLines,
            "non_json_lines" to counts.nonJsonLines,
            "missing_core_fields" to counts.missingCoreFields,
            "missing_end_fields" to counts.missingEndFields,
            "bad_duration_ms" to counts.badDurationMs,
            "type_counts" to counts.typeCounts,
            "platform_counts" to counts.platformCounts,
            "mode" to mode,
            "total_violations" to totalViolations,
            "failing_violations" to failingViolations,
            "offenders" to offenders.map {
                mapOf(
                    "line" to it.line,
                    "reason" to it.reason,
                    "sample" to it.sample,
                )
            },
        )

        return TestLogRunResult(
            exitCode = if (failingViolations > 0) 1 else 0,
            outputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(report),
        )
    }

    private fun parseValidateOptions(args: List<String>): ValidateOptions {
        val filePath = args.firstOrNull { !it.startsWith("--") } ?: DEFAULT_LOG_PATH
        val maxOffenders = args.firstOrNull { it.startsWith("--max-offenders=") }
            ?.substringAfter('=')
            ?.toIntOrNull()
            ?: DEFAULT_MAX_OFFENDERS

        return ValidateOptions(
            filePath = filePath,
            maxOffenders = maxOffenders,
            strictMode = args.contains("--strict"),
            failOnNonJson = args.contains("--fail-on-non-json"),
            failOnMissingCore = args.contains("--fail-on-missing-core"),
        )
    }

    private fun splitJsonlLikeNode(content: String): List<String> {
        val lines = mutableListOf<String>()
        var index = 0
        while (index <= content.length) {
            val nextLf = content.indexOf('\n', index)
            if (nextLf < 0) {
                lines.add(content.substring(index))
                break
            }

            val endIndex = if (nextLf > index && content[nextLf - 1] == '\r') nextLf - 1 else nextLf
            lines.add(content.substring(index, endIndex))
            index = nextLf + 1

            if (index == content.length) {
                lines.add("")
                break
            }
        }
        return lines
    }

    private fun missingKeys(parsed: JsonNode, keys: List<String>): List<String> = keys.filter { key ->
        val value = parsed.get(key)
        value == null || value.isNull || (value.isTextual && value.asText().isEmpty())
    }

    private fun addCount(bucket: MutableMap<String, Int>, value: JsonNode?) {
        val label = when {
            value == null || value.isNull -> "undefined"
            value.isTextual && value.asText().isEmpty() -> "undefined"
            value.isTextual -> value.asText()
            else -> value.toString()
        }
        bucket[label] = (bucket[label] ?: 0) + 1
    }

    private fun addOffender(
        offenders: MutableList<Offender>,
        maxOffenders: Int,
        lineNumber: Int,
        reason: String,
        sample: String,
    ) {
        if (offenders.size < maxOffenders) {
            offenders.add(
                Offender(
                    line = lineNumber,
                    reason = reason,
                    sample = sample.take(160),
                ),
            )
        }
    }

    private data class ValidateOptions(
        val filePath: String,
        val maxOffenders: Int,
        val strictMode: Boolean,
        val failOnNonJson: Boolean,
        val failOnMissingCore: Boolean,
    )

    private data class ValidateCounts(
        val file: String,
        val totalLines: Int,
        var nonEmptyLines: Int = 0,
        var parsedJsonLines: Int = 0,
        var nonJsonLines: Int = 0,
        var missingCoreFields: Int = 0,
        var missingEndFields: Int = 0,
        var badDurationMs: Int = 0,
        val typeCounts: MutableMap<String, Int> = linkedMapOf(),
        val platformCounts: MutableMap<String, Int> = linkedMapOf(),
    )

    private data class Offender(
        val line: Int,
        val reason: String,
        val sample: String,
    )
}
