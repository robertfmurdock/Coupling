import kotlin.js.JsName

interface Whatever : CreatePairCandidateReportActionDispatcher, CreateAllPairCandidateReportsActionDispatcher


@JsName("actionDispatcherMock")
fun actionDispatcherMock(): CreatePairCandidateReportActionDispatcher =
        object : CreatePairCandidateReportActionDispatcher,
                CreateAllPairCandidateReportsActionDispatcher,
                GetNextPairActionDispatcher {

            override val actionDispatcher: Whatever get() = TODO("not implemented")

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

            override fun CreateAllPairCandidateReportsAction.perform(): List<PairCandidateReport> {
                return reportsToReturn
            }


            var nextReportsToReturn: List<PairCandidateReport> = emptyList()

            @JsName("setNextPairCandidateReportsToReturn")
            fun setNextPairCandidateReportsToReturn(reports: Array<PairCandidateReportJs>) {
                nextReportsToReturn = reports.map { fromJsReport(it) }
            }

            var lastGetNextPairAction: MutableList<GetNextPairAction> = mutableListOf()

            @JsName("getPlayersReturnedFromGetNextPairActionAtIndex")
            fun getPlayersReturnedFromGetNextPairActionAtIndex(index: Int) =
                    lastGetNextPairAction.getOrNull(index)
                            ?.gameSpin?.remainingPlayers?.toTypedArray()

            override fun GetNextPairAction.perform(): PairCandidateReport? {
                lastGetNextPairAction.add(this)
                val take = nextReportsToReturn.getOrNull(0)
                if (take != null) {
                    nextReportsToReturn = nextReportsToReturn.subList(1, nextReportsToReturn.size)
                }
                return take
            }

        }

data class WhenGivenReturn(val player: Player, val allPlayers: List<Player>, val returnValue: PairCandidateReportJs)

interface PairCandidateReportJs {

    val player: Player
    val partnerCandidates: Array<Player>
    val timeSinceLastPaired: Int?

}
