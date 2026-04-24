package com.zegreatrob.coupling.testlog.analysis

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

class AnalyzeSourceScanParityTest {
    private val mapper = ObjectMapper()

    @Test
    fun `source scan includes Test kt suites with testmints import`() = setup(object {
        val tempRoot = createTempDirectory(prefix = "analyze-source-scan-")
        val sourceFile = (tempRoot / "src" / "jvmTest" / "kotlin" / "com" / "example" / "IncludedSuiteTest.kt")
            .also {
                it.parent.createDirectories()
                it.writeText(
                    """
                    package com.example
                    import com.zegreatrob.testmints.setup
                    class IncludedSuiteTest
                    """.trimIndent(),
                )
            }
        val file = writeTempJsonl(
            """
            {"type":"TestStart","timestamp":"2026-04-23T01:02:03Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.IncludedSuiteTest","test":"works"}
            {"type":"TestEnd","timestamp":"2026-04-23T01:02:04Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.IncludedSuiteTest","test":"works","status":"SUCCESS","duration_ms":12}
            """.trimIndent(),
        )
    }) exercise {
        withUserDir(tempRoot.pathString) {
            TestLogTools.run(TestLogRequest(TestLogCommand.ANALYZE, listOf("--strict", file.toString())))
        }
    } verify { result ->
        val report = mapper.readTree(requireNotNull(result.outputJson))
        result.exitCode.assertIsEqualTo(1)
        report["source_suites_using_testmints"].asInt().assertIsEqualTo(1)
        report["expected_testmints_tests"].asInt().assertIsEqualTo(1)
        report["tests_missing_expected_testmints"].asInt().assertIsEqualTo(1)
        tempRoot.toFile().deleteRecursively()
    }

    @Test
    fun `source scan skips build and node modules directories`() = setup(object {
        val tempRoot = createTempDirectory(prefix = "analyze-source-scan-")

        val buildFile = (tempRoot / "build" / "tmp" / "IgnoredSuiteTest.kt")
            .also {
                it.parent.createDirectories()
                it.writeText(
                    """
                    package com.example
                    import com.zegreatrob.testmints.setup
                    class IgnoredSuiteTest
                    """.trimIndent(),
                )
            }

        val nodeModulesFile = (tempRoot / "node_modules" / "foo" / "IgnoredNodeSuiteTest.kt")
            .also {
                it.parent.createDirectories()
                it.writeText(
                    """
                    package com.example
                    import com.zegreatrob.testmints.setup
                    class IgnoredNodeSuiteTest
                    """.trimIndent(),
                )
            }

        val file = writeTempJsonl(
            """
            {"type":"TestStart","timestamp":"2026-04-23T01:02:03Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.IgnoredSuiteTest","test":"works"}
            {"type":"TestEnd","timestamp":"2026-04-23T01:02:04Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.IgnoredSuiteTest","test":"works","status":"SUCCESS","duration_ms":12}
            """.trimIndent(),
        )
    }) exercise {
        withUserDir(tempRoot.pathString) {
            TestLogTools.run(TestLogRequest(TestLogCommand.ANALYZE, listOf("--strict", file.toString())))
        }
    } verify { result ->
        val report = mapper.readTree(requireNotNull(result.outputJson))
        result.exitCode.assertIsEqualTo(0)
        report["source_suites_using_testmints"].asInt().assertIsEqualTo(0)
        report["expected_testmints_tests"].asInt().assertIsEqualTo(0)
        report["tests_missing_expected_testmints"].asInt().assertIsEqualTo(0)
        tempRoot.toFile().deleteRecursively()
    }

    private fun writeTempJsonl(content: String) = createTempFile(prefix = "analyze-source-scan-", suffix = ".jsonl")
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
