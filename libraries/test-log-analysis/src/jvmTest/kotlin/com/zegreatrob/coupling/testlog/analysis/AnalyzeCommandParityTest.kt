package com.zegreatrob.coupling.testlog.analysis

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.createTempFile
import kotlin.io.path.div
import kotlin.io.path.pathString
import kotlin.io.path.writeText
import kotlin.test.Test

class AnalyzeCommandParityTest {
    private val mapper = ObjectMapper()

    @Test
    fun `report mode emits violations but does not fail build`() = setup(object {
        val file = writeTempJsonl(
            """
            {"type":"TestEnd","timestamp":"2026-04-23T01:02:03Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.NonTestMintsSuite","test":"works","status":"SUCCESS","duration_ms":12}
            {"type":"TestStart","timestamp":"2026-04-23T01:02:04Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.NonTestMintsSuite","test":"dup","status":"SUCCESS"}
            {"type":"TestStart","timestamp":"2026-04-23T01:02:05Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.NonTestMintsSuite","test":"dup","status":"SUCCESS"}
            """.trimIndent(),
        )
    }) exercise {
        TestLogTools.run(
            TestLogRequest(
                command = TestLogCommand.ANALYZE,
                args = listOf(file.toString()),
            ),
        )
    } verify { result ->
        val report = parseOutput(result)

        result.exitCode.assertIsEqualTo(0)
        report["mode"].asText().assertIsEqualTo("report")
        report["tests_missing_start"].asInt().assertIsEqualTo(1)
        report["tests_missing_end"].asInt().assertIsEqualTo(1)
        report["tests_with_duplicate_start"].asInt().assertIsEqualTo(1)
        report["total_violations"].asInt().assertIsEqualTo(3)
        report["failing_violations"].asInt().assertIsEqualTo(0)
    }

    @Test
    fun `strict mode fails when expected testmints test has no testmints events`() = setup(object {
        val tempRoot = createTempDirectory(prefix = "analyze-source-root-")
        val sourceFile = (tempRoot / "src" / "test" / "kotlin" / "com" / "example" / "ExpectedTestmintsSuiteTest.kt")
            .also {
                it.parent.createDirectories()
                it.writeText(
                    """
                    package com.example
                    import com.zegreatrob.testmints.setup
                    class ExpectedTestmintsSuiteTest
                    """.trimIndent(),
                )
            }
        val file = writeTempJsonl(
            """
            {"type":"TestStart","timestamp":"2026-04-23T01:02:03Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.ExpectedTestmintsSuiteTest","test":"works"}
            {"type":"TestEnd","timestamp":"2026-04-23T01:02:04Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.ExpectedTestmintsSuiteTest","test":"works","status":"SUCCESS","duration_ms":12}
            """.trimIndent(),
        )
    }) exercise {
        withUserDir(tempRoot.pathString) {
            TestLogTools.run(
                TestLogRequest(
                    command = TestLogCommand.ANALYZE,
                    args = listOf("--strict", file.toString()),
                ),
            )
        }
    } verify { result ->
        val report = parseOutput(result)

        result.exitCode.assertIsEqualTo(1)
        report["mode"].asText().assertIsEqualTo("strict")
        report["expected_testmints_tests"].asInt().assertIsEqualTo(1)
        report["tests_missing_expected_testmints"].asInt().assertIsEqualTo(1)
        report["total_violations"].asInt().assertIsEqualTo(1)
        report["failing_violations"].asInt().assertIsEqualTo(1)
        tempRoot.toFile().deleteRecursively()
    }

    @Test
    fun `strict mode passes when required testmints phases are present`() = setup(object {
        val tempRoot = createTempDirectory(prefix = "analyze-source-root-")
        val sourceFile = (tempRoot / "src" / "test" / "kotlin" / "com" / "example" / "RequiredPhasesSuiteTest.kt")
            .also {
                it.parent.createDirectories()
                it.writeText(
                    """
                    package com.example
                    import com.zegreatrob.testmints.setup
                    class RequiredPhasesSuiteTest
                    """.trimIndent(),
                )
            }
        val file = writeTempJsonl(
            """
            {"type":"TestStart","timestamp":"2026-04-23T01:02:03Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.RequiredPhasesSuiteTest","test":"works"}
            {"type":"Log","timestamp":"2026-04-23T01:02:03Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.RequiredPhasesSuiteTest","test":"works","logger":"testmints","properties":{"testmints_phase":"setup-start"}}
            {"type":"Log","timestamp":"2026-04-23T01:02:03Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.RequiredPhasesSuiteTest","test":"works","logger":"testmints","properties":{"testmints_phase":"setup-finish"}}
            {"type":"Log","timestamp":"2026-04-23T01:02:03Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.RequiredPhasesSuiteTest","test":"works","logger":"testmints","properties":{"testmints_phase":"exercise-start"}}
            {"type":"Log","timestamp":"2026-04-23T01:02:03Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.RequiredPhasesSuiteTest","test":"works","logger":"testmints","properties":{"testmints_phase":"exercise-finish"}}
            {"type":"Log","timestamp":"2026-04-23T01:02:03Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.RequiredPhasesSuiteTest","test":"works","logger":"testmints","properties":{"testmints_phase":"verify-start"}}
            {"type":"Log","timestamp":"2026-04-23T01:02:03Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.RequiredPhasesSuiteTest","test":"works","logger":"testmints","properties":{"testmints_phase":"verify-finish"}}
            {"type":"TestEnd","timestamp":"2026-04-23T01:02:04Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.RequiredPhasesSuiteTest","test":"works","status":"SUCCESS","duration_ms":12}
            """.trimIndent(),
        )
    }) exercise {
        withUserDir(tempRoot.pathString) {
            TestLogTools.run(
                TestLogRequest(
                    command = TestLogCommand.ANALYZE,
                    args = listOf("--strict", file.toString()),
                ),
            )
        }
    } verify { result ->
        val report = parseOutput(result)

        result.exitCode.assertIsEqualTo(0)
        report["mode"].asText().assertIsEqualTo("strict")
        report["tests_with_testmints"].asInt().assertIsEqualTo(1)
        report["expected_testmints_tests"].asInt().assertIsEqualTo(1)
        report["tests_missing_expected_testmints"].asInt().assertIsEqualTo(0)
        report["tests_missing_required_testmints_phases"].asInt().assertIsEqualTo(0)
        report["phase_counts"]["setup-start"].asInt().assertIsEqualTo(1)
        report["phase_counts"]["verify-finish"].asInt().assertIsEqualTo(1)
        tempRoot.toFile().deleteRecursively()
    }

    @Test
    fun `strict mode includes e2e task testmints phases when suite and test are present`() = setup(object {
        val tempRoot = createTempDirectory(prefix = "analyze-source-root-")
        val sourceFile = (tempRoot / "e2e" / "src" / "jsE2eTest" / "kotlin" / "com" / "example" / "WelcomeE2ETest.kt")
            .also {
                it.parent.createDirectories()
                it.writeText(
                    """
                    package com.example
                    import com.zegreatrob.testmints.async.asyncTestTemplate
                    class WelcomeE2ETest
                    """.trimIndent(),
                )
            }
        val file = writeTempJsonl(
            """
            {"type":"TestStart","timestamp":"2026-04-23T01:02:03Z","run_id":"r1","platform":"e2e","task":":e2e:e2eRun","suite":"com.example.WelcomeE2ETest","test":"welcome renders"}
            {"type":"Log","timestamp":"2026-04-23T01:02:03Z","run_id":"r1","platform":"e2e","task":":e2e:e2eRun","suite":"com.example.WelcomeE2ETest","test":"welcome renders","logger":"testmints","properties":{"testmints_phase":"setup-start"}}
            {"type":"Log","timestamp":"2026-04-23T01:02:03Z","run_id":"r1","platform":"e2e","task":":e2e:e2eRun","suite":"com.example.WelcomeE2ETest","test":"welcome renders","logger":"testmints","properties":{"testmints_phase":"setup-finish"}}
            {"type":"Log","timestamp":"2026-04-23T01:02:03Z","run_id":"r1","platform":"e2e","task":":e2e:e2eRun","suite":"com.example.WelcomeE2ETest","test":"welcome renders","logger":"testmints","properties":{"testmints_phase":"exercise-start"}}
            {"type":"Log","timestamp":"2026-04-23T01:02:03Z","run_id":"r1","platform":"e2e","task":":e2e:e2eRun","suite":"com.example.WelcomeE2ETest","test":"welcome renders","logger":"testmints","properties":{"testmints_phase":"exercise-finish"}}
            {"type":"Log","timestamp":"2026-04-23T01:02:03Z","run_id":"r1","platform":"e2e","task":":e2e:e2eRun","suite":"com.example.WelcomeE2ETest","test":"welcome renders","logger":"testmints","properties":{"testmints_phase":"verify-start"}}
            {"type":"Log","timestamp":"2026-04-23T01:02:03Z","run_id":"r1","platform":"e2e","task":":e2e:e2eRun","suite":"com.example.WelcomeE2ETest","test":"welcome renders","logger":"testmints","properties":{"testmints_phase":"verify-finish"}}
            {"type":"TestEnd","timestamp":"2026-04-23T01:02:04Z","run_id":"r1","platform":"e2e","task":":e2e:e2eRun","suite":"com.example.WelcomeE2ETest","test":"welcome renders","status":"SUCCESS","duration_ms":12}
            """.trimIndent(),
        )
    }) exercise {
        withUserDir(tempRoot.pathString) {
            TestLogTools.run(
                TestLogRequest(
                    command = TestLogCommand.ANALYZE,
                    args = listOf("--strict", file.toString()),
                ),
            )
        }
    } verify { result ->
        val report = parseOutput(result)
        result.exitCode.assertIsEqualTo(0)
        report["mode"].asText().assertIsEqualTo("strict")
        report["phase_counts"]["setup-start"].asInt().assertIsEqualTo(1)
        report["phase_counts"]["verify-finish"].asInt().assertIsEqualTo(1)
        report["tests_missing_required_testmints_phases"].asInt().assertIsEqualTo(0)
        tempRoot.toFile().deleteRecursively()
    }

    @Test
    fun `analyze reports parseable command timing metrics for canonical command logs`() = setup(object {
        val file = writeTempJsonl(
            """
            {"type":"TestStart","timestamp":"2026-04-23T01:02:03Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.CommandSuite","test":"capturesCommandDurations"}
            {"type":"Log","timestamp":"2026-04-23T01:02:03.100Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.CommandSuite","test":"capturesCommandDurations","logger":"command","message":"opaque","properties":{"command":true,"command_action":"GqlQuery","command_phase":"start","command_trace_id":"t1"}}
            {"type":"Log","timestamp":"2026-04-23T01:02:03.300Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.CommandSuite","test":"capturesCommandDurations","logger":"command","message":"opaque","properties":{"command":true,"command_action":"GqlQuery","command_phase":"end","command_trace_id":"t1","command_duration_ms":120.500}}
            {"type":"Log","timestamp":"2026-04-23T01:02:03.350Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.CommandSuite","test":"capturesCommandDurations","logger":"command","message":"opaque","properties":{"command":true,"command_action":"DeletePartyCommand","command_phase":"start","command_trace_id":"t2"}}
            {"type":"Log","timestamp":"2026-04-23T01:02:03.650Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.CommandSuite","test":"capturesCommandDurations","logger":"command","message":"opaque","properties":{"command":true,"command_action":"DeletePartyCommand","command_phase":"end","command_trace_id":"t2","command_duration_ms":250.0}}
            {"type":"TestEnd","timestamp":"2026-04-23T01:02:04Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.CommandSuite","test":"capturesCommandDurations","status":"SUCCESS","duration_ms":1000}
            """.trimIndent(),
        )
    }) exercise {
        TestLogTools.run(
            TestLogRequest(
                command = TestLogCommand.ANALYZE,
                args = listOf(file.toString()),
            ),
        )
    } verify { result ->
        val report = parseOutput(result)

        result.exitCode.assertIsEqualTo(0)
        report["command_log_events_total"].asInt().assertIsEqualTo(4)
        report["command_canonical_events_total"].asInt().assertIsEqualTo(4)
        report["command_log_events_parsed"].asInt().assertIsEqualTo(4)
        report["command_start_events"].asInt().assertIsEqualTo(2)
        report["command_end_events"].asInt().assertIsEqualTo(2)
        report["command_end_events_with_duration"].asInt().assertIsEqualTo(2)
        report["command_unique_actions"].asInt().assertIsEqualTo(2)
        report["command_events_by_task"][":sdk:jvmTest"].asInt().assertIsEqualTo(4)
        report["command_duration_ms_by_action"]["GqlQuery"]["max_ms"].asDouble().assertIsEqualTo(120.5)
        report["command_duration_ms_by_action"]["DeletePartyCommand"]["max_ms"].asDouble().assertIsEqualTo(250.0)
        report["slowest_command_actions_by_task"][":sdk:jvmTest"].size().assertIsEqualTo(2)
        report["slowest_command_actions_by_task"][":sdk:jvmTest"][0]["action"].asText()
            .assertIsEqualTo("DeletePartyCommand")
        report["slowest_command_actions_by_platform"]["jvm"][0]["action"].asText()
            .assertIsEqualTo("DeletePartyCommand")
        report["tests_with_command_timings"].asInt().assertIsEqualTo(1)
        report["tests_command_time_share_p50"].asDouble().assertIsEqualTo(0.371)
        report["top_tests_by_command_time_share"].size().assertIsEqualTo(1)
        report["top_tests_by_command_time_share"][0]["share"].asDouble().assertIsEqualTo(0.371)
        report["top_tests_by_command_time_share"][0]["command_duration_ms"].asDouble().assertIsEqualTo(370.5)
    }

    @Test
    fun `analyze emits per-task per-platform rollups and top test shares`() = setup(object {
        val file = writeTempJsonl(
            """
            {"type":"TestStart","timestamp":"2026-04-23T01:02:03Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.RollupSuite","test":"slowJvm"}
            {"type":"Log","timestamp":"2026-04-23T01:02:03.100Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.RollupSuite","test":"slowJvm","logger":"command","properties":{"command":true,"command_action":"JvmCommand","command_phase":"start","command_trace_id":"j1"}}
            {"type":"Log","timestamp":"2026-04-23T01:02:03.300Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.RollupSuite","test":"slowJvm","logger":"command","properties":{"command":true,"command_action":"JvmCommand","command_phase":"end","command_trace_id":"j1","command_duration_ms":500.0}}
            {"type":"TestEnd","timestamp":"2026-04-23T01:02:04Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.RollupSuite","test":"slowJvm","status":"SUCCESS","duration_ms":1000}
            {"type":"TestStart","timestamp":"2026-04-23T01:02:05Z","run_id":"r2","platform":"js","task":":sdk:jsNodeTest","suite":"com.example.RollupSuite","test":"slowerShareJs"}
            {"type":"Log","timestamp":"2026-04-23T01:02:05.100Z","run_id":"r2","platform":"js","task":":sdk:jsNodeTest","suite":"com.example.RollupSuite","test":"slowerShareJs","logger":"command","properties":{"command":true,"command_action":"JsCommand","command_phase":"start","command_trace_id":"s1"}}
            {"type":"Log","timestamp":"2026-04-23T01:02:05.200Z","run_id":"r2","platform":"js","task":":sdk:jsNodeTest","suite":"com.example.RollupSuite","test":"slowerShareJs","logger":"command","properties":{"command":true,"command_action":"JsCommand","command_phase":"end","command_trace_id":"s1","command_duration_ms":300.0}}
            {"type":"TestEnd","timestamp":"2026-04-23T01:02:06Z","run_id":"r2","platform":"js","task":":sdk:jsNodeTest","suite":"com.example.RollupSuite","test":"slowerShareJs","status":"SUCCESS","duration_ms":400}
            """.trimIndent(),
        )
    }) exercise {
        TestLogTools.run(
            TestLogRequest(
                command = TestLogCommand.ANALYZE,
                args = listOf(file.toString()),
            ),
        )
    } verify { result ->
        val report = parseOutput(result)

        result.exitCode.assertIsEqualTo(0)
        report["slowest_command_actions_by_task"][":sdk:jvmTest"][0]["action"].asText().assertIsEqualTo("JvmCommand")
        report["slowest_command_actions_by_task"][":sdk:jsNodeTest"][0]["action"].asText().assertIsEqualTo("JsCommand")
        report["slowest_command_actions_by_platform"]["jvm"][0]["max_ms"].asDouble().assertIsEqualTo(500.0)
        report["slowest_command_actions_by_platform"]["js"][0]["max_ms"].asDouble().assertIsEqualTo(300.0)
        report["top_tests_by_command_time_share"].size().assertIsEqualTo(2)
        report["top_tests_by_command_time_share"][0]["task"].asText().assertIsEqualTo(":sdk:jsNodeTest")
        report["top_tests_by_command_time_share"][0]["share"].asDouble().assertIsEqualTo(0.75)
        report["top_tests_by_command_time_share"][1]["task"].asText().assertIsEqualTo(":sdk:jvmTest")
        report["top_tests_by_command_time_share"][1]["share"].asDouble().assertIsEqualTo(0.5)
    }

    @Test
    fun `analyze prefers canonical command properties without message parsing`() = setup(object {
        val file = writeTempJsonl(
            """
            {"type":"TestStart","timestamp":"2026-04-23T01:02:03Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.CanonicalCommandSuite","test":"readsStructuredCommandFields"}
            {"type":"Log","timestamp":"2026-04-23T01:02:03.100Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.CanonicalCommandSuite","test":"readsStructuredCommandFields","logger":"command","message":"opaque","properties":{"command":true,"command_action":"SpinCommand","command_phase":"start","command_trace_id":"c1"}}
            {"type":"Log","timestamp":"2026-04-23T01:02:03.300Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.CanonicalCommandSuite","test":"readsStructuredCommandFields","logger":"command","message":"opaque","properties":{"command":true,"command_action":"SpinCommand","command_phase":"end","command_trace_id":"c1","command_duration_ms":175.25}}
            {"type":"TestEnd","timestamp":"2026-04-23T01:02:04Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.CanonicalCommandSuite","test":"readsStructuredCommandFields","status":"SUCCESS","duration_ms":1000}
            """.trimIndent(),
        )
    }) exercise {
        TestLogTools.run(
            TestLogRequest(
                command = TestLogCommand.ANALYZE,
                args = listOf(file.toString()),
            ),
        )
    } verify { result ->
        val report = parseOutput(result)

        result.exitCode.assertIsEqualTo(0)
        report["command_log_events_total"].asInt().assertIsEqualTo(2)
        report["command_canonical_events_total"].asInt().assertIsEqualTo(2)
        report["command_log_events_parsed"].asInt().assertIsEqualTo(2)
        report["command_parse_failures_by_task"].size().assertIsEqualTo(0)
        report["command_contract_violations"].asInt().assertIsEqualTo(0)
        report["command_duration_ms_by_action"]["SpinCommand"]["max_ms"].asDouble().assertIsEqualTo(175.25)
        report["tests_with_command_timings"].asInt().assertIsEqualTo(1)
    }

    @Test
    fun `strict mode fails when canonical command fields are malformed`() = setup(object {
        val file = writeTempJsonl(
            """
            {"type":"TestStart","timestamp":"2026-04-23T01:02:03Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.ContractSuite","test":"invalidCanonicalCommand"}
            {"type":"Log","timestamp":"2026-04-23T01:02:03.100Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.ContractSuite","test":"invalidCanonicalCommand","logger":"command","message":"opaque","properties":{"command":true,"command_action":"SpinCommand","command_phase":"finish","command_duration_ms":"175ms"}}
            {"type":"TestEnd","timestamp":"2026-04-23T01:02:04Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.ContractSuite","test":"invalidCanonicalCommand","status":"SUCCESS","duration_ms":1000}
            """.trimIndent(),
        )
    }) exercise {
        TestLogTools.run(
            TestLogRequest(
                command = TestLogCommand.ANALYZE,
                args = listOf("--strict", file.toString()),
            ),
        )
    } verify { result ->
        val report = parseOutput(result)

        result.exitCode.assertIsEqualTo(1)
        report["mode"].asText().assertIsEqualTo("strict")
        report["command_canonical_events_total"].asInt().assertIsEqualTo(1)
        report["command_contract_violations"].asInt().assertIsEqualTo(3)
        report["command_contract_violations_by_task"][":sdk:jvmTest"].asInt().assertIsEqualTo(3)
        report["total_violations"].asInt().assertIsEqualTo(3)
        report["failing_violations"].asInt().assertIsEqualTo(3)
    }

    @Test
    fun `analyze ignores legacy action logger message payloads`() = setup(object {
        val file = writeTempJsonl(
            """
            {"type":"TestStart","timestamp":"2026-04-23T01:02:03Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.LegacySuite","test":"legacyActionLoggerLine"}
            {"type":"Log","timestamp":"2026-04-23T01:02:03.100Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.LegacySuite","test":"legacyActionLoggerLine","logger":"ActionLogger","message":"{action=SpinCommand, type=Start, traceId=t1}"}
            {"type":"Log","timestamp":"2026-04-23T01:02:03.300Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.LegacySuite","test":"legacyActionLoggerLine","logger":"forwarded-output","message":"[DefaultDispatcher-worker-2] INFO ActionLogger - {action=SpinCommand, type=End, duration=175ms, traceId=t1}"}
            {"type":"TestEnd","timestamp":"2026-04-23T01:02:04Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.LegacySuite","test":"legacyActionLoggerLine","status":"SUCCESS","duration_ms":1000}
            """.trimIndent(),
        )
    }) exercise {
        TestLogTools.run(
            TestLogRequest(
                command = TestLogCommand.ANALYZE,
                args = listOf(file.toString()),
            ),
        )
    } verify { result ->
        val report = parseOutput(result)

        result.exitCode.assertIsEqualTo(0)
        report["command_log_events_total"].asInt().assertIsEqualTo(0)
        report["command_log_events_parsed"].asInt().assertIsEqualTo(0)
        report["command_contract_violations"].asInt().assertIsEqualTo(0)
        report["tests_with_command_timings"].asInt().assertIsEqualTo(0)
    }

    private fun parseOutput(result: TestLogRunResult): JsonNode = mapper.readTree(requireNotNull(result.outputJson))

    private fun writeTempJsonl(content: String) = createTempFile(prefix = "analyze-parity-", suffix = ".jsonl")
        .also { it.writeText(content) }

    private fun <T> withUserDir(path: String, block: () -> T): T {
        val old = System.getProperty("user.dir")
        return try {
            System.setProperty("user.dir", path)
            block()
        } finally {
            System.setProperty("user.dir", old)
        }
    }
}
