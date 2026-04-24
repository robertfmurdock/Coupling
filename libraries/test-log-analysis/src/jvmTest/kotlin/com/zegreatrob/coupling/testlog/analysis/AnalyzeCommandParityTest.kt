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
