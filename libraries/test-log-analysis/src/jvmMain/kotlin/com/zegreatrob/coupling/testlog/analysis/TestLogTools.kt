package com.zegreatrob.coupling.testlog.analysis

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.time.Instant

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
    private const val TOP_SLOW_COMMANDS_PER_SCOPE = 5
    private const val TOP_TEST_COMMAND_SHARE_LIMIT = 10
    private val commandAttributionRequiredTasks = setOf(
        ":sdk:jvmTest",
        ":sdk:jsNodeTest",
        ":e2e:e2eRun",
    )
    val defaultValidateParityKeys = listOf(
        "total_lines",
        "non_empty_lines",
        "parsed_json_lines",
        "non_json_lines",
        "missing_core_fields",
        "missing_end_fields",
        "bad_duration_ms",
        "command_missing_canonical_fields",
        "command_bad_phase",
        "command_bad_duration_ms",
        "command_missing_test_attribution_fields",
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

            if (type == "Log") {
                val contractViolations = canonicalCommandContractViolations(parsed)
                if (contractViolations.missingFields > 0) {
                    counts.commandMissingCanonicalFields += 1
                }
                if (contractViolations.badPhase > 0) {
                    counts.commandBadPhase += 1
                }
                if (contractViolations.badDurationMs > 0) {
                    counts.commandBadDurationMs += 1
                }
                contractViolations.reasons.forEach { reason ->
                    addOffender(offenders, options.maxOffenders, index + 1, "command contract: $reason", line)
                }

                val commandAttributionViolations = commandTestAttributionViolations(parsed)
                if (commandAttributionViolations.isNotEmpty()) {
                    counts.commandMissingTestAttributionFields += 1
                    commandAttributionViolations.forEach { reason ->
                        addOffender(offenders, options.maxOffenders, index + 1, "command attribution: $reason", line)
                    }
                }
            }
        }

        val totalViolations =
            counts.nonJsonLines +
                counts.missingCoreFields +
                counts.missingEndFields +
                counts.badDurationMs +
                counts.commandMissingCanonicalFields +
                counts.commandBadPhase +
                counts.commandBadDurationMs +
                counts.commandMissingTestAttributionFields

        val failingViolations = if (options.strictMode) {
            totalViolations
        } else {
            (if (options.failOnNonJson) counts.nonJsonLines else 0) +
                (if (options.failOnMissingCore) counts.missingCoreFields else 0) +
                (if (options.failOnMissingEnd) counts.missingEndFields else 0) +
                (if (options.failOnBadDuration) counts.badDurationMs else 0)
        }

        val mode = if (options.strictMode) {
            "strict"
        } else {
            val failCategories = buildList {
                if (options.failOnNonJson) add("non-json")
                if (options.failOnMissingCore) add("core")
                if (options.failOnMissingEnd) add("end")
                if (options.failOnBadDuration) add("duration")
            }
            if (failCategories.isEmpty()) {
                "compat"
            } else {
                "compat-fail-${failCategories.joinToString("-")}"
            }
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
            "command_missing_canonical_fields" to counts.commandMissingCanonicalFields,
            "command_bad_phase" to counts.commandBadPhase,
            "command_bad_duration_ms" to counts.commandBadDurationMs,
            "command_missing_test_attribution_fields" to counts.commandMissingTestAttributionFields,
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
            failOnMissingEnd = args.contains("--fail-on-missing-end"),
            failOnBadDuration = args.contains("--fail-on-bad-duration"),
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
        val commandMetrics = CommandMetrics()

        var parsedJsonLines = 0
        var nonJsonLines = 0
        var testStartCount = 0
        var testEndCount = 0
        var commandCanonicalEvents = 0
        var commandContractViolations = 0
        val commandContractViolationsByTask = linkedMapOf<String, Int>()
        var commandMissingTestAttributionFields = 0
        val commandMissingTestAttributionFieldsByTask = linkedMapOf<String, Int>()

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
                record.durationMs = event.get("duration_ms")?.asDouble()
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

            if (event.get("type")?.asText() == "Log") {
                val commandResult = processCommandLogEvent(event, key, commandMetrics)
                if (commandResult.canonicalEvent) {
                    commandCanonicalEvents += 1
                }
                if (commandResult.attributionViolationReasons.isNotEmpty()) {
                    val task = event.get("task")?.asText()?.ifEmpty { null } ?: "unknown-task"
                    commandMissingTestAttributionFields += commandResult.attributionViolationReasons.size
                    commandMissingTestAttributionFieldsByTask[task] =
                        (commandMissingTestAttributionFieldsByTask[task] ?: 0) + commandResult.attributionViolationReasons.size
                    commandResult.attributionViolationReasons.forEach { reason ->
                        addAnalyzeOffender(
                            offenders,
                            options.maxOffenders,
                            "command-attribution-$reason $task",
                        )
                    }
                }
                if (commandResult.contractViolationReasons.isNotEmpty()) {
                    val task = event.get("task")?.asText()?.ifEmpty { null } ?: "unknown-task"
                    commandContractViolations += commandResult.contractViolationReasons.size
                    commandContractViolationsByTask[task] =
                        (commandContractViolationsByTask[task] ?: 0) + commandResult.contractViolationReasons.size
                    commandResult.contractViolationReasons.forEach { reason ->
                        addAnalyzeOffender(
                            offenders,
                            options.maxOffenders,
                            "command-contract-$reason $task",
                        )
                    }
                }
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

        val totalViolations =
            missingStart +
                missingEnd +
                duplicateStart +
                duplicateEnd +
                missingExpectedTestMints +
                missingPhaseTests +
                commandContractViolations +
                commandMissingTestAttributionFields
        val commandSummary = commandMetrics.summary()
        val commandEventsInAttributionScope = commandMetrics.commandEventsInAttributionScope
        val commandEventsWithFullTestAttribution = commandMetrics.commandEventsWithFullTestAttribution
        val commandEventsMissingAnyTestAttribution =
            (commandEventsInAttributionScope - commandEventsWithFullTestAttribution).coerceAtLeast(0)
        val commandEventsWithFullTestAttributionRatio = if (commandEventsInAttributionScope == 0) {
            1.0
        } else {
            (commandEventsWithFullTestAttribution.toDouble() / commandEventsInAttributionScope.toDouble()).roundTo3()
        }
        val testCommandShareDetails = tests.mapNotNull { (key, record) ->
            val duration = record.durationMs
            val commandDuration = commandMetrics.testCommandDurationsMs[key]
            if (duration == null || duration <= 0.0 || commandDuration == null) {
                null
            } else {
                TestCommandShare(
                    runId = record.runId,
                    task = record.task,
                    suite = record.suite,
                    test = record.test,
                    testId = record.testId,
                    testDurationMs = duration.roundTo3(),
                    commandDurationMs = commandDuration.roundTo3(),
                    share = (commandDuration / duration).coerceAtLeast(0.0).roundTo3(),
                )
            }
        }
        val testCommandShares = testCommandShareDetails.map { it.share }
        val topTestsByCommandTimeShare = testCommandShareDetails
            .sortedByDescending { it.share }
            .take(TOP_TEST_COMMAND_SHARE_LIMIT)
            .map { detail ->
                mapOf(
                    "run_id" to detail.runId,
                    "task" to detail.task,
                    "suite" to detail.suite,
                    "test" to detail.test,
                    "test_id" to detail.testId,
                    "share" to detail.share,
                    "command_duration_ms" to detail.commandDurationMs,
                    "test_duration_ms" to detail.testDurationMs,
                )
            }
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
            "command_log_events_total" to commandSummary.logEventsTotal,
            "command_canonical_events_total" to commandCanonicalEvents,
            "command_log_events_parsed" to commandSummary.logEventsParsed,
            "command_start_events" to commandSummary.startEvents,
            "command_end_events" to commandSummary.endEvents,
            "command_end_events_with_duration" to commandSummary.endEventsWithDuration,
            "command_unique_actions" to commandSummary.actionCount,
            "command_events_by_task" to commandSummary.eventsByTask,
            "command_parse_failures_by_task" to commandSummary.parseFailuresByTask,
            "command_contract_violations" to commandContractViolations,
            "command_contract_violations_by_task" to commandContractViolationsByTask,
            "command_missing_test_attribution_fields" to commandMissingTestAttributionFields,
            "command_missing_test_attribution_fields_by_task" to commandMissingTestAttributionFieldsByTask,
            "command_events_in_attribution_scope" to commandEventsInAttributionScope,
            "command_events_with_full_test_attribution" to commandEventsWithFullTestAttribution,
            "command_events_missing_any_test_attribution" to commandEventsMissingAnyTestAttribution,
            "command_events_with_full_test_attribution_ratio" to commandEventsWithFullTestAttributionRatio,
            "command_duration_ms_by_action" to commandSummary.durationByAction,
            "slowest_command_actions" to commandSummary.slowestActions,
            "slowest_command_actions_by_task" to commandSummary.slowestActionsByTask,
            "slowest_command_actions_by_platform" to commandSummary.slowestActionsByPlatform,
            "tests_with_command_timings" to testCommandShares.size,
            "tests_command_time_share_p50" to percentile(testCommandShares, 0.5).roundTo3(),
            "tests_command_time_share_p95" to percentile(testCommandShares, 0.95).roundTo3(),
            "top_tests_by_command_time_share" to topTestsByCommandTimeShare,
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
        val testId = event.get("test_id")?.asText()?.takeIf { it.isNotEmpty() }
        val task = event.get("task")?.asText()?.takeIf { it.isNotEmpty() }
        val suite = event.get("suite")?.asText()?.takeIf { it.isNotEmpty() }
        val test = event.get("test")?.asText()?.takeIf { it.isNotEmpty() }
        val runId = event.get("run_id")?.asText()?.takeIf { it.isNotEmpty() }
        return if (task == null || runId == null) {
            null
        } else if (testId != null) {
            "$runId||$task||$testId"
        } else if (suite != null && test != null) {
            "$runId||$task||$suite||$test"
        } else {
            null
        }
    }

    private fun commandTestAttributionKey(event: JsonNode): String? {
        val runId = event.get("run_id")?.asText()?.takeIf { it.isNotEmpty() } ?: return null
        val task = event.get("task")?.asText()?.takeIf { it.isNotEmpty() } ?: return null
        val properties = event.get("properties")?.takeIf { it.isObject } ?: return null
        val testId = properties.get("test_id")?.asText()?.takeIf { it.isNotBlank() }
        if (testId != null) {
            return "$runId||$task||$testId"
        }
        val suite = properties.get("test_suite")?.asText()?.takeIf { it.isNotBlank() }
        val test = properties.get("test_name")?.asText()?.takeIf { it.isNotBlank() }
        return if (suite != null && test != null) "$runId||$task||$suite||$test" else null
    }

    private fun makeAnalyzeRecord(event: JsonNode) = AnalyzeTestRecord(
        runId = event.get("run_id")?.asText()?.ifEmpty { null } ?: "unknown-run",
        task = event.get("task")?.asText()?.ifEmpty { null } ?: "unknown-task",
        suite = event.get("suite")?.asText()?.ifEmpty { null } ?: "unknown-suite",
        test = event.get("test")?.asText()?.ifEmpty { null } ?: "unknown-test",
        testId = event.get("test_id")?.asText()?.ifEmpty { null },
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

    private fun processCommandLogEvent(
        event: JsonNode,
        testKey: String?,
        metrics: CommandMetrics,
    ): CommandProcessResult {
        val task = event.get("task")?.asText()?.ifEmpty { null } ?: "unknown-task"
        val platform = event.get("platform")?.asText()?.ifEmpty { null } ?: "unknown-platform"
        if (!mightContainCommandData(event)) {
            return CommandProcessResult()
        }
        val contractViolations = canonicalCommandContractViolations(event)
        val attributionViolations = commandTestAttributionViolations(event)
        val requiresAttribution = isCommandAttributionRequiredTask(event.get("task")?.asText()) && contractViolations.isCanonicalCommandEvent
        val hasCompleteAttribution = attributionViolations.isEmpty()
        fun buildProcessResult() = CommandProcessResult(
            canonicalEvent = contractViolations.isCanonicalCommandEvent,
            contractViolationReasons = contractViolations.reasons,
            attributionViolationReasons = attributionViolations,
            requiresAttribution = requiresAttribution,
            hasCompleteAttribution = hasCompleteAttribution,
        )

        recordCommandAttributionCoverage(
            metrics = metrics,
            requiresAttribution = requiresAttribution,
            hasCompleteAttribution = hasCompleteAttribution,
        )
        metrics.logEventsTotal += 1
        val timestampEpochMs = event.get("timestamp")?.asText()?.let { parseInstantToEpochMs(it) }
        val parsedCommand = parseCommandEvent(event) ?: run {
            metrics.parseFailuresByTask[task] = (metrics.parseFailuresByTask[task] ?: 0) + 1
            return buildProcessResult()
        }

        metrics.logEventsParsed += 1
        metrics.eventsByTask[task] = (metrics.eventsByTask[task] ?: 0) + 1
        metrics.actionEventCount[parsedCommand.action] = (metrics.actionEventCount[parsedCommand.action] ?: 0) + 1

        val commandKey = listOf(
            event.get("run_id")?.asText().orEmpty(),
            task,
            commandTestAttributionKey(event) ?: "",
            parsedCommand.traceId ?: "",
            parsedCommand.action,
        ).joinToString("||")

        val phase = parsedCommand.phase.lowercase()
        if (phase == "start") {
            metrics.startEvents += 1
            if (timestampEpochMs != null) {
                val starts = metrics.pendingStarts.getOrPut(commandKey) { ArrayDeque() }
                starts.addLast(timestampEpochMs)
            }
            return buildProcessResult()
        }

        if (phase != "end") {
            return buildProcessResult()
        }

        metrics.endEvents += 1
        val durationMs = parsedCommand.durationMs ?: run {
            val starts = metrics.pendingStarts[commandKey]
            val startEpochMs = starts?.removeLastOrNull()
            if (timestampEpochMs != null && startEpochMs != null) {
                (timestampEpochMs - startEpochMs).coerceAtLeast(0.0)
            } else {
                null
            }
        }

        if (durationMs == null) {
            return buildProcessResult()
        }

        metrics.endEventsWithDuration += 1
        val durations = metrics.actionDurationsMs.getOrPut(parsedCommand.action) { mutableListOf() }
        durations += durationMs
        metrics.addScopedDuration(metrics.taskActionDurationsMs, task, parsedCommand.action, durationMs)
        metrics.addScopedDuration(metrics.platformActionDurationsMs, platform, parsedCommand.action, durationMs)

        val attributedTestKey = commandTestAttributionKey(event) ?: testKey
        if (attributedTestKey != null) {
            metrics.testCommandDurationsMs[attributedTestKey] = (metrics.testCommandDurationsMs[attributedTestKey] ?: 0.0) + durationMs
        }

        return buildProcessResult()
    }

    private fun recordCommandAttributionCoverage(
        metrics: CommandMetrics,
        requiresAttribution: Boolean,
        hasCompleteAttribution: Boolean,
    ) {
        if (!requiresAttribution) {
            return
        }
        metrics.commandEventsInAttributionScope += 1
        if (hasCompleteAttribution) {
            metrics.commandEventsWithFullTestAttribution += 1
        }
    }

    private fun mightContainCommandData(event: JsonNode): Boolean {
        val logger = event.get("logger")?.asText() ?: ""
        if (logger == "command") {
            return true
        }
        val properties = event.get("properties")
        return properties != null && properties.isObject && properties.get("command_action") != null
    }

    private fun parseCommandEvent(event: JsonNode): ParsedCommandEvent? {
        val properties = event.get("properties")

        val propertyAction = properties?.get("command_action")?.asText()?.takeIf { it.isNotBlank() }
        val propertyPhase = properties?.get("command_phase")?.asText()?.takeIf { it.isNotBlank() }
        if (propertyAction != null && propertyPhase != null) {
            val propertyTraceId = properties?.get("command_trace_id")?.asText()?.takeIf { it.isNotBlank() }
            val propertyDurationMs = properties?.get("command_duration_ms")?.let { node ->
                if (node.isNumber) node.asDouble() else null
            }
            return ParsedCommandEvent(
                action = propertyAction,
                phase = propertyPhase.lowercase(),
                traceId = propertyTraceId,
                durationMs = propertyDurationMs,
            )
        }
        return null
    }

    private fun canonicalCommandContractViolations(event: JsonNode): CommandContractViolations {
        if (!isCanonicalCommandEvent(event)) {
            return CommandContractViolations()
        }

        val reasons = mutableListOf<String>()
        var missingFields = 0
        var badPhase = 0
        var badDurationMs = 0

        val properties = event.get("properties")
        if (properties == null || !properties.isObject) {
            reasons += "missing-properties"
            return CommandContractViolations(
                isCanonicalCommandEvent = true,
                missingFields = 1,
                reasons = reasons,
            )
        }

        val action = properties.get("command_action")?.asText()?.takeIf { it.isNotBlank() }
        if (action == null) {
            missingFields += 1
            reasons += "missing-command_action"
        }

        val phaseRaw = properties.get("command_phase")?.asText()?.takeIf { it.isNotBlank() }
        val phase = phaseRaw?.lowercase()
        if (phase == null) {
            missingFields += 1
            reasons += "missing-command_phase"
        } else if (phase != "start" && phase != "end") {
            badPhase += 1
            reasons += "invalid-command_phase"
        }

        val traceId = properties.get("command_trace_id")?.asText()?.takeIf { it.isNotBlank() }
        if (traceId == null) {
            missingFields += 1
            reasons += "missing-command_trace_id"
        }

        val duration = properties.get("command_duration_ms")
        val isErrorEvent = properties.get("command_error")?.asBoolean() == true
        if (phase == "end" && !isErrorEvent) {
            if (duration != null && !duration.isNull && !duration.isNumber) {
                badDurationMs += 1
                reasons += "non-numeric-command_duration_ms"
            }
        } else if (duration != null && !duration.isNull && !duration.isNumber) {
            badDurationMs += 1
            reasons += "non-numeric-command_duration_ms"
        }

        return CommandContractViolations(
            isCanonicalCommandEvent = true,
            missingFields = missingFields,
            badPhase = badPhase,
            badDurationMs = badDurationMs,
            reasons = reasons,
        )
    }

    private fun commandTestAttributionViolations(event: JsonNode): List<String> {
        if (!isCanonicalCommandEvent(event)) {
            return emptyList()
        }
        if (!isCommandAttributionRequiredTask(event.get("task")?.asText())) {
            return emptyList()
        }
        val properties = event.get("properties")
        if (properties == null || !properties.isObject) {
            return listOf("missing-properties")
        }
        val reasons = mutableListOf<String>()
        val testSuite = properties.get("test_suite")?.asText()?.takeIf { it.isNotBlank() }
        if (testSuite == null) {
            reasons += "missing-test_suite"
        }
        val testName = properties.get("test_name")?.asText()?.takeIf { it.isNotBlank() }
        if (testName == null) {
            reasons += "missing-test_name"
        }
        val testId = properties.get("test_id")?.asText()?.takeIf { it.isNotBlank() }
        if (testId == null) {
            reasons += "missing-test_id"
        }
        return reasons
    }

    private fun isCanonicalCommandEvent(event: JsonNode): Boolean {
        val logger = event.get("logger")?.asText()
        if (logger == "command") {
            return true
        }
        val properties = event.get("properties")
        if (properties == null || !properties.isObject) {
            return false
        }
        if (properties.get("command")?.asBoolean() == true) {
            return true
        }
        return properties.fieldNames().asSequence().any { it.startsWith("command_") }
    }

    private fun isCommandAttributionRequiredTask(task: String?): Boolean {
        val normalized = task?.takeIf { it.isNotBlank() } ?: return false
        return commandAttributionRequiredTasks.contains(normalized)
    }

    private fun parseInstantToEpochMs(value: String): Double? = runCatching {
        Instant.parse(value).toEpochMilli().toDouble()
    }.getOrNull()

    private fun percentile(values: List<Double>, percentile: Double): Double {
        if (values.isEmpty()) {
            return 0.0
        }
        val sorted = values.sorted()
        val clamped = percentile.coerceIn(0.0, 1.0)
        val index = ((sorted.size - 1) * clamped).toInt()
        return sorted[index]
    }

    private fun Double.roundTo3(): Double = String.format("%.3f", this).toDouble()

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
        val failOnMissingEnd: Boolean,
        val failOnBadDuration: Boolean,
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
        var commandMissingCanonicalFields: Int = 0,
        var commandBadPhase: Int = 0,
        var commandBadDurationMs: Int = 0,
        var commandMissingTestAttributionFields: Int = 0,
        val typeCounts: MutableMap<String, Int> = linkedMapOf(),
        val platformCounts: MutableMap<String, Int> = linkedMapOf(),
    )

    private data class Offender(
        val line: Int,
        val reason: String,
        val sample: String,
    )

    private data class AnalyzeTestRecord(
        val runId: String,
        val task: String,
        val suite: String,
        val test: String,
        val testId: String? = null,
        var starts: Int = 0,
        var ends: Int = 0,
        var status: String? = null,
        var durationMs: Double? = null,
        var hasTestMints: Boolean = false,
        val phases: MutableSet<String> = mutableSetOf(),
    )

    private data class ParsedCommandEvent(
        val action: String,
        val phase: String,
        val traceId: String?,
        val durationMs: Double?,
    )

    private data class CommandProcessResult(
        val canonicalEvent: Boolean = false,
        val contractViolationReasons: List<String> = emptyList(),
        val attributionViolationReasons: List<String> = emptyList(),
        val requiresAttribution: Boolean = false,
        val hasCompleteAttribution: Boolean = false,
    )

    private data class CommandContractViolations(
        val isCanonicalCommandEvent: Boolean = false,
        val missingFields: Int = 0,
        val badPhase: Int = 0,
        val badDurationMs: Int = 0,
        val reasons: List<String> = emptyList(),
    )

    private data class CommandSummary(
        val logEventsTotal: Int,
        val logEventsParsed: Int,
        val startEvents: Int,
        val endEvents: Int,
        val endEventsWithDuration: Int,
        val actionCount: Int,
        val eventsByTask: Map<String, Int>,
        val parseFailuresByTask: Map<String, Int>,
        val durationByAction: Map<String, Map<String, Double>>,
        val slowestActions: List<Map<String, Any>>,
        val slowestActionsByTask: Map<String, List<Map<String, Any>>>,
        val slowestActionsByPlatform: Map<String, List<Map<String, Any>>>,
    )

    private class CommandMetrics {
        var logEventsTotal: Int = 0
        var logEventsParsed: Int = 0
        var startEvents: Int = 0
        var endEvents: Int = 0
        var endEventsWithDuration: Int = 0
        var commandEventsInAttributionScope: Int = 0
        var commandEventsWithFullTestAttribution: Int = 0
        val eventsByTask: MutableMap<String, Int> = linkedMapOf()
        val parseFailuresByTask: MutableMap<String, Int> = linkedMapOf()
        val actionEventCount: MutableMap<String, Int> = linkedMapOf()
        val actionDurationsMs: MutableMap<String, MutableList<Double>> = linkedMapOf()
        val taskActionDurationsMs: MutableMap<String, MutableMap<String, MutableList<Double>>> = linkedMapOf()
        val platformActionDurationsMs: MutableMap<String, MutableMap<String, MutableList<Double>>> = linkedMapOf()
        val pendingStarts: MutableMap<String, ArrayDeque<Double>> = mutableMapOf()
        val testCommandDurationsMs: MutableMap<String, Double> = mutableMapOf()

        fun addScopedDuration(
            scopedDurations: MutableMap<String, MutableMap<String, MutableList<Double>>>,
            scope: String,
            action: String,
            durationMs: Double,
        ) {
            val actionDurations = scopedDurations.getOrPut(scope) { linkedMapOf() }
            val durations = actionDurations.getOrPut(action) { mutableListOf() }
            durations += durationMs
        }

        fun summary(): CommandSummary {
            val durationByAction = actionDurationsMs.mapValues { (_, durations) -> durationStats(durations) }
            val slowestActions = slowestActionRollup(actionDurationsMs, 10)
            val slowestActionsByTask = scopedSlowestActionRollup(taskActionDurationsMs)
            val slowestActionsByPlatform = scopedSlowestActionRollup(platformActionDurationsMs)

            return CommandSummary(
                logEventsTotal = logEventsTotal,
                logEventsParsed = logEventsParsed,
                startEvents = startEvents,
                endEvents = endEvents,
                endEventsWithDuration = endEventsWithDuration,
                actionCount = actionDurationsMs.size,
                eventsByTask = eventsByTask,
                parseFailuresByTask = parseFailuresByTask,
                durationByAction = durationByAction,
                slowestActions = slowestActions,
                slowestActionsByTask = slowestActionsByTask,
                slowestActionsByPlatform = slowestActionsByPlatform,
            )
        }

        private fun durationStats(durations: List<Double>): Map<String, Double> {
            val count = durations.size.toDouble()
            val total = durations.sum()
            return mapOf(
                "count" to count,
                "total_ms" to total.roundTo3(),
                "avg_ms" to if (count > 0) (total / count).roundTo3() else 0.0,
                "p50_ms" to percentile(durations, 0.5).roundTo3(),
                "p95_ms" to percentile(durations, 0.95).roundTo3(),
                "max_ms" to durations.maxOrNull()?.roundTo3().orZero(),
            )
        }

        private fun slowestActionRollup(
            actionDurations: Map<String, List<Double>>,
            limit: Int,
        ): List<Map<String, Any>> = actionDurations.entries
            .sortedByDescending { (_, durations) -> durations.maxOrNull() ?: 0.0 }
            .take(limit)
            .map { (action, durations) ->
                mapOf(
                    "action" to action,
                    "count" to durations.size,
                    "max_ms" to durations.maxOrNull()?.roundTo3().orZero(),
                    "p95_ms" to percentile(durations, 0.95).roundTo3(),
                    "avg_ms" to if (durations.isEmpty()) 0.0 else (durations.sum() / durations.size).roundTo3(),
                )
            }

        private fun scopedSlowestActionRollup(
            scopedDurations: Map<String, Map<String, List<Double>>>,
        ): Map<String, List<Map<String, Any>>> {
            val result = linkedMapOf<String, List<Map<String, Any>>>()
            scopedDurations.keys.sorted().forEach { scope ->
                val actionDurations = scopedDurations[scope].orEmpty()
                result[scope] = slowestActionRollup(actionDurations, TOP_SLOW_COMMANDS_PER_SCOPE)
            }
            return result
        }
    }

    private fun Double?.orZero(): Double = this ?: 0.0

    private data class TestCommandShare(
        val runId: String,
        val task: String,
        val suite: String,
        val test: String,
        val testId: String?,
        val testDurationMs: Double,
        val commandDurationMs: Double,
        val share: Double,
    )
}
