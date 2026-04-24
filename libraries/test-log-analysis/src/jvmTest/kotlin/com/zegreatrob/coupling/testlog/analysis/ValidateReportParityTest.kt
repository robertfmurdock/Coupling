package com.zegreatrob.coupling.testlog.analysis

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class ValidateReportParityTest {
    @Test
    fun `compareValidateReports matches when key metrics are equal`() = setup(object {
        val expectedReport =
            """
            {
              "total_lines": 10,
              "non_empty_lines": 8,
              "parsed_json_lines": 8,
              "non_json_lines": 0,
              "missing_core_fields": 0,
              "missing_end_fields": 0,
              "bad_duration_ms": 0,
              "total_violations": 0,
              "failing_violations": 0,
              "mode": "compat-fail-non-json-core",
              "offenders": []
            }
            """.trimIndent()
        val actualReport =
            """
            {
              "total_lines": 10,
              "non_empty_lines": 8,
              "parsed_json_lines": 8,
              "non_json_lines": 0,
              "missing_core_fields": 0,
              "missing_end_fields": 0,
              "bad_duration_ms": 0,
              "total_violations": 0,
              "failing_violations": 0,
              "mode": "compat-fail-non-json-core",
              "offenders": [{"line":1,"reason":"ignored","sample":"x"}]
            }
            """.trimIndent()
    }) exercise {
        TestLogTools.compareValidateReports(
            expectedReportJson = expectedReport,
            actualReportJson = actualReport,
        )
    } verify { result ->
        result.matches.assertIsEqualTo(true)
        result.mismatches.size.assertIsEqualTo(0)
    }

    @Test
    fun `compareValidateReports returns mismatches when key metrics differ`() = setup(object {
        val expectedReport =
            """
            {
              "total_lines": 10,
              "non_empty_lines": 8,
              "parsed_json_lines": 8,
              "non_json_lines": 0,
              "missing_core_fields": 0,
              "missing_end_fields": 0,
              "bad_duration_ms": 0,
              "total_violations": 0,
              "failing_violations": 0,
              "mode": "strict"
            }
            """.trimIndent()
        val actualReport =
            """
            {
              "total_lines": 11,
              "non_empty_lines": 8,
              "parsed_json_lines": 7,
              "non_json_lines": 1,
              "missing_core_fields": 0,
              "missing_end_fields": 0,
              "bad_duration_ms": 0,
              "total_violations": 1,
              "failing_violations": 1,
              "mode": "compat-fail-non-json-core"
            }
            """.trimIndent()
    }) exercise {
        TestLogTools.compareValidateReports(
            expectedReportJson = expectedReport,
            actualReportJson = actualReport,
        )
    } verify { result ->
        result.matches.assertIsEqualTo(false)
        result.mismatches.size.assertIsEqualTo(6)
        result.mismatches.map { it.key }.contains("total_lines").assertIsEqualTo(true)
        result.mismatches.map { it.key }.contains("parsed_json_lines").assertIsEqualTo(true)
        result.mismatches.map { it.key }.contains("non_json_lines").assertIsEqualTo(true)
        result.mismatches.map { it.key }.contains("total_violations").assertIsEqualTo(true)
        result.mismatches.map { it.key }.contains("failing_violations").assertIsEqualTo(true)
        result.mismatches.map { it.key }.contains("mode").assertIsEqualTo(true)
    }
}
