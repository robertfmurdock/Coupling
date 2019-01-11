import kotlin.js.JsName

@JsName("actionDispatcherMock")
fun actionDispatcherMock(): CreatePairCandidateReportActionDispatcher =
        object : CreatePairCandidateReportActionDispatcher, CreateAllPairCandidateReportsCommandDispatcher {
            override val actionDispatcher: CreatePairCandidateReportActionDispatcher get() = TODO("not implemented")

            val whenGivenReturnList: MutableList<WhenGivenReturn> = mutableListOf()

            @JsName("whenGiven")
            fun whenGiven(player: Player, allPlayers: Array<Player>, returnValue: PairCandidateReportJs) {
                whenGivenReturnList.add(
                        WhenGivenReturn(player, allPlayers.toList(), returnValue)
                )
            }

            override val couplingComparisionSyntax: CouplingComparisionSyntax get() = TODO("not implemented")

            override fun CreatePairCandidateReportAction.perform(): PairCandidateReport {
                return whenGivenReturnList.find {
                    this.allPlayers == it.allPlayers
                            && this.player == it.player
                }?.returnValue
                        ?.let { fromJsReport(it) }
                        ?: throw NotImplementedError("Test condition not set up. $this")
            }

            private fun fromJsReport(it: PairCandidateReportJs): PairCandidateReport {
                return PairCandidateReport(
                        it.player,
                        it.partnerCandidates.toList(),
                        it.timeSinceLastPaired?.let { time -> TimeResultValue(time) }
                                ?: NeverPaired

                )
            }

            @JsName("setPairCandidateReportsToReturn")
            fun setPairCandidateReportsToReturn(reports: Array<PairCandidateReportJs>) {
                reportsToReturn = reports.map { fromJsReport(it) }
            }

            var reportsToReturn: List<PairCandidateReport> = emptyList()

            override fun CreateAllPairCandidateReportsCommand.perform(): List<PairCandidateReport> {
                return reportsToReturn
            }
        }

data class WhenGivenReturn(val player: Player, val allPlayers: List<Player>, val returnValue: PairCandidateReportJs)

interface PairCandidateReportJs {

    val player: Player
    val partnerCandidates: Array<Player>
    val timeSinceLastPaired: Int?

}
