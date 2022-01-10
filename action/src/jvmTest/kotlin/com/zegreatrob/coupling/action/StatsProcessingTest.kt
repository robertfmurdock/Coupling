package com.zegreatrob.coupling.action

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.soywiz.klock.DateTime
import com.soywiz.klock.Year
import java.io.File
import kotlin.test.Ignore
import kotlin.test.Test

class StatsProcessingTest {

    @Test
    @Ignore
    fun generateStats() {
        val objectMapper = ObjectMapper()

        val users = mutableListOf<JsonNode>()

        val tribes = mutableListOf<JsonNode>()

        File("${System.getenv()["PWD"]}/prod.202201100933.log").forEachLine {
            val tree = objectMapper.readTree(it)
            if (tree["userEmail"] != null) {
                users.add(tree)
            } else if (tree["tribeId"] != null) {
                tribes.add(tree)
            }
        }

        println("found ${users.size} users")
        println("found ${tribes.size} tribes")


        val totalPairAssignmentRecords = tribes.sumOf { tribeNode ->
            tribeNode["pairAssignmentRecords"]
                .groupBy { pairAssignmentRecord -> pairAssignmentRecord["id"].textValue() }
                .size
        }
        println("found $totalPairAssignmentRecords unique pair assignment records")

        (2019..2022).forEach { year ->
            val pairAssignmentRecordsYear = tribes.map { tribeNode ->
                tribeNode.countPairAssignmentsFor(year)
            }

            println("found ${pairAssignmentRecordsYear.sum()} unique pair assignment records in $year")

            (1..10).forEach { index ->
                val filter = index * 5
                val count = pairAssignmentRecordsYear.filter { it > filter }.size
                if (count > 0)
                    println("found $count tribes with more than $filter in $year")
            }

        }

    }

    private fun JsonNode.countPairAssignmentsFor(year: Int) = pairAssignmentRecordGroups(year)
        .also { pairRecordGroups ->
            if (pairRecordGroups.size > 20) {
                val tribeName = this["tribeRecords"][0]["name"].textValue()
                val tribeId = this["tribeRecords"][0]["id"].textValue()
                println("notable tribe in $year: $tribeName, $tribeId")
                val sortedDates = pairRecordGroups.map { group -> group.value.first().dateRecord() }
                    .sorted()
                println("started in ${sortedDates.first().month} and last spin in ${sortedDates.last().month}")
            }
        }.size

    private fun JsonNode.pairAssignmentRecordGroups(year: Int) = this["pairAssignmentRecords"]
        .groupBy { pairAssignmentRecord -> pairAssignmentRecord["id"].textValue() }
        .filter { group ->
            val date = group.value.first().dateRecord()
            date.year == Year(year)
        }

    private fun JsonNode.dateRecord() = DateTime.parse(this["date"].textValue())

}