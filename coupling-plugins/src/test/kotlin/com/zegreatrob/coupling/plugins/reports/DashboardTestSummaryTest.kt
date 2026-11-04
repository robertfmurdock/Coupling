package com.zegreatrob.coupling.plugins.reports

import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DashboardTestSummaryTest {
    @Test
    fun `summary binds combined JUnit outcomes to the workflow run`() {
        val summary = dashboardTestSummary(
            listOf(
                """<testsuite tests="99"><testcase name="pass"/><testcase name="fail"><failure/></testcase></testsuite>""",
                """<testsuites><testsuite><testcase name="skip"><skipped/></testcase><testcase name="error"><error/></testcase></testsuite></testsuites>""",
            ),
            runId = 123,
            runAttempt = 2,
            headSha = "abc123",
        )

        assertEquals(
            ObjectMapper().readTree("""{"schemaVersion":1,"runId":123,"runAttempt":2,"headSha":"abc123","total":4,"passed":1,"failed":2,"skipped":1}"""),
            ObjectMapper().readTree(summary),
        )
    }

    @Test
    fun `missing reports cannot masquerade as zero tests`() {
        assertFailsWith<IllegalArgumentException> { dashboardTestSummary(emptyList(), 123, 1, "abc") }
    }

    @Test
    fun `malformed reports cannot publish partial totals`() {
        assertFailsWith<org.xml.sax.SAXParseException> {
            dashboardTestSummary(listOf("<testsuite>"), 123, 1, "abc")
        }
    }

    @Test
    fun `summary requires valid workflow identity`() {
        assertFailsWith<IllegalArgumentException> {
            dashboardTestSummary(listOf("<testsuite/>"), 0, 1, "abc")
        }
    }
}
