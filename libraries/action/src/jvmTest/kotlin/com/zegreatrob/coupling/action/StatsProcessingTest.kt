package com.zegreatrob.coupling.action

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.File
import kotlin.math.roundToInt
import kotlin.test.Ignore
import kotlin.test.Test

class StatsProcessingTest {

    @Test
    @Ignore
    fun generateStats() {
        val objectMapper = ObjectMapper()

        val users = mutableListOf<JsonNode>()

        val parties = mutableListOf<JsonNode>()

        File("${System.getenv()["PWD"]}/prod.202201100933.log").forEachLine {
            val tree = objectMapper.readTree(it)
            if (tree["userEmail"] != null) {
                users.add(tree)
            } else if (tree["tribeId"] != null) {
                parties.add(tree)
            }
        }

        println("found ${users.size} users")
        println("found ${parties.size} parties")

        val totalPairAssignmentRecords = parties.sumOf { partyNode ->
            partyNode["pairAssignmentRecords"]
                .groupBy { pairAssignmentRecord -> pairAssignmentRecord["id"].textValue() }
                .size
        }
        println("found $totalPairAssignmentRecords unique pair assignment records")

        (2019..2022).forEach { year ->
            val pairAssignmentRecordsYear = parties.map { partyNode ->
                partyNode.countPairAssignmentsFor(year)
            }

            println("found ${pairAssignmentRecordsYear.sum()} unique pair assignment records in $year")

            (1..10).forEach { index ->
                val filter = index * 5
                val count = pairAssignmentRecordsYear.filter { it > filter }.size
                if (count > 0) {
                    println("found $count parties with more than $filter in $year")
                }
            }
        }
    }

    private fun JsonNode.countPairAssignmentsFor(year: Int) = pairAssignmentRecordGroups(year)
        .also { pairRecordGroups ->
            if (pairRecordGroups.size > 20) {
                val partyName = this["tribeRecords"][0]["name"].textValue()
                val partyId = this["tribeRecords"][0]["id"].textValue()
                println("notable party in $year: $partyName, $partyId")
                val sortedDates = pairRecordGroups.map { group -> group.value.first().dateRecord() }
                    .sorted()

                val playerCount =
                    pairRecordGroups.map { group -> group.value.first()["pairs"].sumOf { pair -> pair["players"].size() } }
                        .average()
                        .roundToInt()

                val pinCount =
                    pairRecordGroups.map { group -> group.value.first()["pairs"].sumOf { pair -> pair["pins"].size() } }
                        .average()

                println("started in ${sortedDates.first().month} and last spin in ${sortedDates.last().month}. average of $playerCount players, average of $pinCount pins")
            }
        }.size

    private fun JsonNode.pairAssignmentRecordGroups(year: Int) = this["pairAssignmentRecords"]
        .groupBy { pairAssignmentRecord -> pairAssignmentRecord["id"].textValue() }
        .filter { group ->
            val date = group.value.first().dateRecord()
            date.year == year
        }

    private fun JsonNode.dateRecord() = Instant.parse(this["date"].textValue()).toLocalDateTime(TimeZone.UTC)
}
