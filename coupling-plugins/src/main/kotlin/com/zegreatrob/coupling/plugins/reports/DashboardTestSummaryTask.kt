package com.zegreatrob.coupling.plugins.reports

import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

abstract class DashboardTestSummaryTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val reports: ConfigurableFileCollection

    @get:Input
    abstract val runId: Property<Long>

    @get:Input
    abstract val runAttempt: Property<Int>

    @get:Input
    abstract val headSha: Property<String>

    @get:OutputFile
    abstract val summaryFile: RegularFileProperty

    @TaskAction
    fun summarize() {
        val output = summaryFile.get().asFile
        output.delete()
        val summary = dashboardTestSummary(reports.files.sorted().map { it.readText() }, runId.get(), runAttempt.get(), headSha.get())
        output.parentFile.mkdirs()
        output.writeText("$summary\n")
    }
}

fun dashboardTestSummary(reports: List<String>, runId: Long, runAttempt: Int, headSha: String): String {
    require(reports.isNotEmpty()) { "No JUnit test reports found; refusing to publish fabricated totals." }
    require(runId > 0 && runAttempt > 0 && headSha.isNotBlank()) { "A valid GitHub run ID, attempt, and SHA are required." }
    val outcomes = reports.flatMap(::junitOutcomes)
    return ObjectMapper().writeValueAsString(
        mapOf(
            "schemaVersion" to 1,
            "runId" to runId,
            "runAttempt" to runAttempt,
            "headSha" to headSha,
            "total" to outcomes.size,
            "passed" to outcomes.count { it == "passed" },
            "failed" to outcomes.count { it == "failed" },
            "skipped" to outcomes.count { it == "skipped" },
        ),
    )
}

private fun junitOutcomes(xml: String): List<String> {
    val factory = DocumentBuilderFactory.newInstance().apply {
        setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
    }
    val cases = factory.newDocumentBuilder().parse(InputSource(StringReader(xml))).getElementsByTagName("testcase")
    return (0 until cases.length).map { index ->
        val case = cases.item(index) as Element
        when {
            case.getElementsByTagName("failure").length + case.getElementsByTagName("error").length > 0 -> "failed"
            case.getElementsByTagName("skipped").length > 0 -> "skipped"
            else -> "passed"
        }
    }
}
