import Comparators from "./Comparators";

export default class CouplingGame {

    constructor(public sequencer, public wheel) {
    }

    private filterOutPlayer(playersOnWheel, playerToFilter) {
        return playersOnWheel.filter(function (player) {
            return !Comparators.areEqualPlayers(player, playerToFilter);
        });
    }

    private spinForAPartner(playersOnWheel, pairHistoryReport, pair, pairs) {
        var furtherRemainingPlayers = this.filterOutPlayer(playersOnWheel, pairHistoryReport.player);
        var partnerCandidates = pairHistoryReport.partnerCandidates;
        if (furtherRemainingPlayers.length > 0) {
            var partner = this.wheel.spin(partnerCandidates);
            pair.push(partner);
            this.spinAndAddPlayerUntilNoPlayersRemain(this.filterOutPlayer(furtherRemainingPlayers, partner), pairs);
        }
    }

    private spinAndAddPlayerUntilNoPlayersRemain(playersOnWheel, pairs) {
        if (playersOnWheel.length == 0) {
            return;
        }
        var pairHistoryReport = this.sequencer.getNextInSequence(playersOnWheel);
        var pair = [pairHistoryReport.player];
        pairs.push(pair);

        this.spinForAPartner(playersOnWheel, pairHistoryReport, pair, pairs);
    }

    public play(playersRoster) {
        var pairs = [];
        this.spinAndAddPlayerUntilNoPlayersRemain(playersRoster, pairs);
        return pairs;
    };
};