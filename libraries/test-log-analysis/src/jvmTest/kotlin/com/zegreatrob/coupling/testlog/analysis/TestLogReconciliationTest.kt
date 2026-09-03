package com.zegreatrob.coupling.testlog.analysis

import com.fasterxml.jackson.databind.ObjectMapper
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.io.path.createTempFile
import kotlin.io.path.readLines
import kotlin.io.path.writeText
import kotlin.test.Test

class TestLogReconciliationTest {
    private val mapper = ObjectMapper()

    @Test
    fun `reconciliation appends one stable incomplete closure and history accepts it`() = setup(object {
        val file = createTempFile(prefix = "reconcile-", suffix = ".jsonl").also {
            it.writeText(
                """
                {"type":"TestEnd","timestamp":"2026-04-23T01:02:03Z","run_id":"incomplete-run","platform":"jvm","duration_ms":"12"}
                """.trimIndent(),
            )
        }
    }) exercise {
        val first = TestLogTools.run(TestLogRequest(TestLogCommand.RECONCILE, listOf(file.toString())))
        val afterFirst = file.readLines()
        val second = TestLogTools.run(TestLogRequest(TestLogCommand.RECONCILE, listOf(file.toString())))
        val history = TestLogTools.run(TestLogRequest(TestLogCommand.VALIDATE_HISTORY, listOf("--strict", file.toString())))
        Triple(first, second, history) to afterFirst
    } verify { (results, afterFirst) ->
        val (first, second, history) = results
        val firstReport = parse(first)
        val secondReport = parse(second)
        val historyReport = parse(history)

        first.exitCode.assertIsEqualTo(0)
        firstReport.get("closures_appended").asInt().assertIsEqualTo(1)
        afterFirst.size.assertIsEqualTo(2)
        afterFirst.last().contains("\"type\":\"RunClosure\"").assertIsEqualTo(true)
        afterFirst.last().contains("\"status\":\"INCOMPLETE\"").assertIsEqualTo(true)
        secondReport.get("closures_appended").asInt().assertIsEqualTo(0)
        history.exitCode.assertIsEqualTo(0)
        historyReport.get("reconciled_violations").asInt().assertIsEqualTo(2)
        historyReport.get("unreconciled_violations").asInt().assertIsEqualTo(0)
    }

    @Test
    fun `history fails only unreconciled violations`() = setup(object {
        val file = createTempFile(prefix = "history-", suffix = ".jsonl").also {
            it.writeText(
                """
                {"type":"TestEnd","timestamp":"2026-04-23T01:02:03Z","run_id":"closed-run","platform":"jvm","duration_ms":"12"}
                {"type":"TestEnd","timestamp":"2026-04-23T01:02:04Z","run_id":"open-run","platform":"jvm","duration_ms":"12"}
                """.trimIndent(),
            )
        }
    }) exercise {
        TestLogTools.run(TestLogRequest(TestLogCommand.RECONCILE, listOf(file.toString())))
        file.writeText(file.readLines().dropLast(1).joinToString("\n") + "\n")
        TestLogTools.run(TestLogRequest(TestLogCommand.VALIDATE_HISTORY, listOf("--strict", file.toString())))
    } verify { history ->
        val report = parse(history)

        history.exitCode.assertIsEqualTo(1)
        report.get("reconciled_violations").asInt().assertIsEqualTo(2)
        report.get("unreconciled_violations").asInt().assertIsEqualTo(2)
    }

    private fun parse(result: TestLogRunResult) = mapper.readTree(requireNotNull(result.outputJson))
}
