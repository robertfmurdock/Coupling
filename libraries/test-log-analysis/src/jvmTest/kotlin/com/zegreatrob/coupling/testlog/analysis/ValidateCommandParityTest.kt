package com.zegreatrob.coupling.testlog.analysis

import com.fasterxml.jackson.databind.ObjectMapper
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText
import kotlin.test.Test

class ValidateCommandParityTest {
    private val mapper = ObjectMapper()

    @Test
    fun `strict mode counts all violations as failing`() = setup(object {
        val file = writeTempJsonl(
            """
            not-json
            {"type":"TestStart"}
            {"type":"TestEnd","timestamp":"2026-04-23T01:02:03Z","run_id":"r1","platform":"jvm","duration_ms":"12"}
            """.trimIndent(),
        )
    }) exercise {
        TestLogTools.run(TestLogRequest(TestLogCommand.VALIDATE, listOf("--strict", file.toString())))
    } verify { result ->
        val json = parseOutput(result)

        result.exitCode.assertIsEqualTo(1)
        json.get("mode").asText().assertIsEqualTo("strict")
        json.get("total_violations").asInt().assertIsEqualTo(4)
        json.get("failing_violations").asInt().assertIsEqualTo(4)
        json.get("non_json_lines").asInt().assertIsEqualTo(1)
        json.get("missing_core_fields").asInt().assertIsEqualTo(1)
        json.get("missing_end_fields").asInt().assertIsEqualTo(1)
        json.get("bad_duration_ms").asInt().assertIsEqualTo(1)
        json.get("command_missing_canonical_fields").asInt().assertIsEqualTo(0)
        json.get("command_bad_phase").asInt().assertIsEqualTo(0)
        json.get("command_bad_duration_ms").asInt().assertIsEqualTo(0)
    }

    @Test
    fun `compat mode with fail flags only fails flagged categories`() = setup(object {
        val file = writeTempJsonl(
            """
            not-json
            {"type":"TestStart"}
            {"type":"TestEnd","timestamp":"2026-04-23T01:02:03Z","run_id":"r1","platform":"jvm","duration_ms":"12"}
            """.trimIndent(),
        )
    }) exercise {
        val nonJsonOnly = TestLogTools.run(
            TestLogRequest(
                TestLogCommand.VALIDATE,
                listOf("--fail-on-non-json", file.toString()),
            ),
        )
        val both = TestLogTools.run(
            TestLogRequest(
                TestLogCommand.VALIDATE,
                listOf("--fail-on-non-json", "--fail-on-missing-core", file.toString()),
            ),
        )
        val phaseC = TestLogTools.run(
            TestLogRequest(
                TestLogCommand.VALIDATE,
                listOf("--fail-on-missing-end", "--fail-on-bad-duration", file.toString()),
            ),
        )
        Triple(nonJsonOnly, both, phaseC)
    } verify { (nonJsonOnly, both, phaseC) ->
        val nonJsonOnlyJson = parseOutput(nonJsonOnly)
        nonJsonOnly.exitCode.assertIsEqualTo(1)
        nonJsonOnlyJson.get("mode").asText().assertIsEqualTo("compat-fail-non-json")
        nonJsonOnlyJson.get("failing_violations").asInt().assertIsEqualTo(1)

        val bothJson = parseOutput(both)
        both.exitCode.assertIsEqualTo(1)
        bothJson.get("mode").asText().assertIsEqualTo("compat-fail-non-json-core")
        bothJson.get("failing_violations").asInt().assertIsEqualTo(2)

        val phaseCJson = parseOutput(phaseC)
        phaseC.exitCode.assertIsEqualTo(1)
        phaseCJson.get("mode").asText().assertIsEqualTo("compat-fail-end-duration")
        phaseCJson.get("failing_violations").asInt().assertIsEqualTo(2)
    }

    @Test
    fun `offenders are capped by max-offenders`() = setup(object {
        val file = writeTempJsonl(
            """
            bad1
            bad2
            bad3
            """.trimIndent(),
        )
    }) exercise {
        TestLogTools.run(
            TestLogRequest(
                TestLogCommand.VALIDATE,
                listOf("--strict", "--max-offenders=2", file.toString()),
            ),
        )
    } verify { result ->
        val json = parseOutput(result)

        result.exitCode.assertIsEqualTo(1)
        json.get("non_json_lines").asInt().assertIsEqualTo(3)
        json.get("offenders").size().assertIsEqualTo(2)
        json.get("offenders").get(0).get("reason").asText().assertIsEqualTo("non-json")
    }

    @Test
    fun `strict mode validates canonical command field contract`() = setup(object {
        val file = writeTempJsonl(
            """
            {"type":"Log","timestamp":"2026-04-23T01:02:03Z","run_id":"r1","platform":"jvm","task":":sdk:jvmTest","suite":"com.example.CommandSuite","test":"contract","logger":"command","message":"opaque","properties":{"command":true,"command_action":"SpinCommand","command_phase":"finish","command_duration_ms":"120ms"}}
            """.trimIndent(),
        )
    }) exercise {
        TestLogTools.run(TestLogRequest(TestLogCommand.VALIDATE, listOf("--strict", file.toString())))
    } verify { result ->
        val json = parseOutput(result)

        result.exitCode.assertIsEqualTo(1)
        json.get("command_missing_canonical_fields").asInt().assertIsEqualTo(1)
        json.get("command_bad_phase").asInt().assertIsEqualTo(1)
        json.get("command_bad_duration_ms").asInt().assertIsEqualTo(1)
        json.get("total_violations").asInt().assertIsEqualTo(3)
        json.get("failing_violations").asInt().assertIsEqualTo(3)
    }

    @Test
    fun `missing file returns exit 2 and expected error message`() = setup(object {
        val missingPath = "/tmp/does-not-exist-${System.nanoTime()}.jsonl"
    }) exercise {
        TestLogTools.run(
            TestLogRequest(TestLogCommand.VALIDATE, listOf(missingPath)),
        )
    } verify { result ->
        result.exitCode.assertIsEqualTo(2)
        result.outputJson.assertIsEqualTo(null)
        (result.errorOutput ?: "").startsWith("ERROR: file not found: /tmp/does-not-exist-").assertIsEqualTo(true)
    }

    private fun parseOutput(result: TestLogRunResult) = mapper.readTree(requireNotNull(result.outputJson))

    private fun writeTempJsonl(content: String) = createTempFile(prefix = "validate-parity-", suffix = ".jsonl")
        .also { it.writeText(content) }
}
