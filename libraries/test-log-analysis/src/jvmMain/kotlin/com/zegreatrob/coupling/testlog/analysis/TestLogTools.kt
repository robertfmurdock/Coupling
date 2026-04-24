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

data class ValidateReportMismatch(
    val key: String,
    val expected: String,
    val actual: String,
)

data class ValidateReportParityResult(
    val matches: Boolean,
    val mismatches: List<ValidateReportMismatch>,
)

object TestLogTools {
    private val mapper = ObjectMapper()
    private const val DEFAULT_LOG_PATH = "build/test-output/test.jsonl"
    private const val DEFAULT_MAX_OFFENDERS = 20
    private const val DEFAULT_ANALYZE_MAX_OFFENDERS = 30
    val defaultValidateParityKeys = listOf(
        "total_lines",
        "non_empty_lines",
        "parsed_json_lines",
        "non_json_lines",
        "missing_core_fields",
        "missing_end_fields",
        "bad_duration_ms",
        "total_violations",
        "failing_violations",
        "mode",
    )
    private val requiredByAny = listOf("type", "timestamp", "run_id", "platform")
    private val requiredByEnd = listOf("status", "duration_ms", "task", "suite", "test")
    private val endEventTypes = setOf("TestEnd", "StepEnd")
    private val knownTestMintsPhases = listOf(
        "setup-start",
        "setup-finish",
        "exercise-start",
        "exercise-finish",
        "verify-start",
        "verify-finish",
        "test-start",
        "test-finish",
    )
    private val requiredTestMintsPhases = listOf(
        "setup-start",
        "setup-finish",
        "exercise-start",
        "exercise-finish",
        "verify-start",
        "verify-finish",
    )
    private val skippedScanDirectories = setOf(".git", ".gradle", "build", "node_modules")

    fun run(request: TestLogRequest): TestLogRunResult = when (request.command) {
        TestLogCommand.VALIDATE -> runValidate(request.args)
        TestLogCommand.ANALYZE -> runAnalyze(request.args)
    }

    fun compareValidateReports(
        expectedReportJson: String,
        actualReportJson: String,
        keys: List<String> = defaultValidateParityKeys,
    ): ValidateReportParityResult {
        val expected = mapper.readTree(expectedReportJson)
        val actual = mapper.readTree(actualReportJson)
        val mismatches = keys.mapNotNull { key ->
            val expectedValue = normalizeForParity(expected.get(key))
            val actualValue = normalizeForParity(actual.get(key))
            if (expectedValue == actualValue) {
                null
            } else {
                ValidateReportMismatch(
                    key = key,
                    expected = expectedValue,
                    actual = actualValue,
                )
            }
        }
        return ValidateReportParityResult(
            matches = mismatches.isEmpty(),
            mismatches = mismatches,
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

    private fun runAnalyze(args: List<String>): TestLogRunResult {
        val options = parseAnalyzeOptions(args)
        val file = File(options.filePath)
        if (!file.exists()) {
            return TestLogRunResult(
                exitCode = 2,
                errorOutput = "ERROR: file not found: ${options.filePath}",
            )
        }

        val lines = splitJsonlLikeNode(file.readText())
        val suiteNamesUsingTestMints = collectTestMintsSuiteNames(File(System.getProperty("user.dir")))
        val tests = linkedMapOf<String, AnalyzeTestRecord>()
        val offenders = mutableListOf<String>()
        val phaseCounts = linkedMapOf<String, Int>().apply {
            knownTestMintsPhases.forEach { put(it, 0) }
        }

        var parsedJsonLines = 0
        var nonJsonLines = 0
        var testStartCount = 0
        var testEndCount = 0

        lines.forEach { line ->
            if (line.isBlank()) {
                return@forEach
            }

            val event = try {
                mapper.readTree(line).also {
                    parsedJsonLines += 1
                }
            } catch (_: Exception) {
                nonJsonLines += 1
                return@forEach
            }

            val key = testKey(event)
            if (event.get("type")?.asText() == "TestStart" && key != null) {
                testStartCount += 1
                val record = tests[key] ?: makeAnalyzeRecord(event)
                record.starts += 1
                tests[key] = record
            }

            if (event.get("type")?.asText() == "TestEnd" && key != null) {
                testEndCount += 1
                val record = tests[key] ?: makeAnalyzeRecord(event)
                record.ends += 1
                record.status = event.get("status")?.asText() ?: record.status
                tests[key] = record
            }

            if (event.get("type")?.asText() == "Log" && key != null && isTestMintsLog(event)) {
                val record = tests[key] ?: makeAnalyzeRecord(event)
                record.hasTestMints = true
                val phase = parseTestMintsPhase(event)
                if (phase != null) {
                    record.phases += phase
                    phaseCounts[phase] = (phaseCounts[phase] ?: 0) + 1
                }
                tests[key] = record
            }
        }

        var missingStart = 0
        var missingEnd = 0
        var duplicateStart = 0
        var duplicateEnd = 0
        var completedTests = 0
        var testsWithTestMints = 0
        var expectedTestMintsTests = 0
        var missingExpectedTestMints = 0
        var missingPhaseTests = 0

        tests.values.forEach { record ->
            if (record.starts == 0) {
                missingStart += 1
                addAnalyzeOffender(
                    offenders,
                    options.maxOffenders,
                    "missing-start ${record.task} ${record.suite}.${record.test}",
                )
            }
            if (record.ends == 0) {
                missingEnd += 1
                addAnalyzeOffender(
                    offenders,
                    options.maxOffenders,
                    "missing-end ${record.task} ${record.suite}.${record.test}",
                )
            }
            if (record.starts > 1) {
                duplicateStart += 1
                addAnalyzeOffender(
                    offenders,
                    options.maxOffenders,
                    "duplicate-start(${record.starts}) ${record.task} ${record.suite}.${record.test}",
                )
            }
            if (record.ends > 1) {
                duplicateEnd += 1
                addAnalyzeOffender(
                    offenders,
                    options.maxOffenders,
                    "duplicate-end(${record.ends}) ${record.task} ${record.suite}.${record.test}",
                )
            }

            val isSkipped = record.status == "SKIPPED"
            if (!isSkipped && record.ends > 0) {
                completedTests += 1
            }

            if (record.hasTestMints) {
                testsWithTestMints += 1
            }

            val suiteSimpleName = record.suite.substringAfterLast('.')
            val expectsTestMints = suiteNamesUsingTestMints.contains(suiteSimpleName)
            if (!isSkipped && record.ends > 0 && expectsTestMints) {
                expectedTestMintsTests += 1
                if (!record.hasTestMints) {
                    missingExpectedTestMints += 1
                    addAnalyzeOffender(
                        offenders,
                        options.maxOffenders,
                        "missing-testmints ${record.task} ${record.suite}.${record.test}",
                    )
                } else {
                    val missingPhases = requiredTestMintsPhases.filterNot { record.phases.contains(it) }
                    if (missingPhases.isNotEmpty()) {
                        missingPhaseTests += 1
                        addAnalyzeOffender(
                            offenders,
                            options.maxOffenders,
                            "missing-phases(${missingPhases.joinToString(",")}) ${record.task} ${record.suite}.${record.test}",
                        )
                    }
                }
            }
        }

        val totalViolations = missingStart + missingEnd + duplicateStart + duplicateEnd + missingExpectedTestMints + missingPhaseTests
        val report = linkedMapOf(
            "file" to options.filePath,
            "mode" to if (options.strictMode) "strict" else "report",
            "parsed_json_lines" to parsedJsonLines,
            "non_json_lines" to nonJsonLines,
            "test_start_events" to testStartCount,
            "test_end_events" to testEndCount,
            "unique_tests" to tests.size,
            "source_suites_using_testmints" to suiteNamesUsingTestMints.size,
            "tests_missing_start" to missingStart,
            "tests_missing_end" to missingEnd,
            "tests_with_duplicate_start" to duplicateStart,
            "tests_with_duplicate_end" to duplicateEnd,
            "completed_tests" to completedTests,
            "tests_with_testmints" to testsWithTestMints,
            "expected_testmints_tests" to expectedTestMintsTests,
            "tests_missing_expected_testmints" to missingExpectedTestMints,
            "tests_missing_required_testmints_phases" to missingPhaseTests,
            "phase_counts" to phaseCounts,
            "total_violations" to totalViolations,
            "failing_violations" to if (options.strictMode) totalViolations else 0,
            "offenders" to offenders,
        )

        return TestLogRunResult(
            exitCode = if (options.strictMode && totalViolations > 0) 1 else 0,
            outputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(report),
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

    private fun parseAnalyzeOptions(args: List<String>): AnalyzeOptions = AnalyzeOptions(
        filePath = args.firstOrNull { !it.startsWith("--") } ?: DEFAULT_LOG_PATH,
        maxOffenders = args.firstOrNull { it.startsWith("--max-offenders=") }
            ?.substringAfter('=')
            ?.toIntOrNull()
            ?: DEFAULT_ANALYZE_MAX_OFFENDERS,
        strictMode = args.contains("--strict"),
    )

    private fun testKey(event: JsonNode): String? {
        val task = event.get("task")?.asText()?.takeIf { it.isNotEmpty() }
        val suite = event.get("suite")?.asText()?.takeIf { it.isNotEmpty() }
        val test = event.get("test")?.asText()?.takeIf { it.isNotEmpty() }
        val runId = event.get("run_id")?.asText()?.takeIf { it.isNotEmpty() }
        return if (task == null || suite == null || test == null || runId == null) {
            null
        } else {
            "$runId||$task||$suite||$test"
        }
    }

    private fun makeAnalyzeRecord(event: JsonNode) = AnalyzeTestRecord(
        task = event.get("task")?.asText()?.ifEmpty { null } ?: "unknown-task",
        suite = event.get("suite")?.asText()?.ifEmpty { null } ?: "unknown-suite",
        test = event.get("test")?.asText()?.ifEmpty { null } ?: "unknown-test",
    )

    private fun isTestMintsLog(event: JsonNode): Boolean {
        if (event.get("logger")?.asText() == "testmints") {
            return true
        }
        val properties = event.get("properties")
        if (properties != null && properties.isObject) {
            if (properties.get("testmints")?.asBoolean() == true) {
                return true
            }
            if (properties.get("testmints_phase")?.asText()?.isNotEmpty() == true) {
                return true
            }
        }
        val message = event.get("message")?.asText() ?: ""
        return message.contains("[testmints]") || Regex("\\btestmints\\b\\s*-").containsMatchIn(message)
    }

    private fun parseTestMintsPhase(event: JsonNode): String? {
        val properties = event.get("properties")
        if (properties != null && properties.isObject) {
            val phase = properties.get("testmints_phase")?.asText()
            if (!phase.isNullOrBlank()) {
                return phase
            }
        }
        val message = event.get("message")?.asText() ?: ""
        return knownTestMintsPhases.firstOrNull { message.contains(it) }
    }

    private fun collectTestMintsSuiteNames(rootDir: File): Set<String> {
        if (!rootDir.exists() || !rootDir.isDirectory) {
            return emptySet()
        }

        val suites = linkedSetOf<String>()

        fun walk(directory: File) {
            if (skippedScanDirectories.contains(directory.name)) {
                return
            }

            directory.listFiles()?.forEach { entry ->
                if (entry.isDirectory) {
                    walk(entry)
                } else if (entry.isFile && entry.name.endsWith("Test.kt")) {
                    val content = runCatching { entry.readText() }.getOrNull() ?: return@forEach
                    if (content.contains("com.zegreatrob.testmints")) {
                        suites += entry.name.removeSuffix(".kt")
                    }
                }
            }
        }

        walk(rootDir)
        return suites
    }

    private fun addAnalyzeOffender(
        offenders: MutableList<String>,
        maxOffenders: Int,
        message: String,
    ) {
        if (offenders.size < maxOffenders) {
            offenders += message
        }
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

    private fun normalizeForParity(value: JsonNode?): String = when {
        value == null || value.isNull -> "null"
        value.isTextual -> value.asText()
        value.isNumber -> value.numberValue().toString()
        value.isBoolean -> value.booleanValue().toString()
        else -> value.toString()
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

    private data class AnalyzeOptions(
        val filePath: String,
        val maxOffenders: Int,
        val strictMode: Boolean,
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

    private data class AnalyzeTestRecord(
        val task: String,
        val suite: String,
        val test: String,
        var starts: Int = 0,
        var ends: Int = 0,
        var status: String? = null,
        var hasTestMints: Boolean = false,
        val phases: MutableSet<String> = mutableSetOf(),
    )
}
