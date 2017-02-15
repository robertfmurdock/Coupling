import Comparators from "./Comparators";

export default class CouplingGame {

    constructor(public sequencer, public wheel) {
    }

    private filterOutPlayer(playersOnWheel, playerToFilter) {
        return playersOnWheel.filter(function (player) {
            return !Comparators.areEqualPlayers(player, playerToFilter);
        });
    }

    private spinForAPartner(playersOnWheel, pairHistoryReport, pair, pairs, pairingRule) {
        const furtherRemainingPlayers = this.filterOutPlayer(playersOnWheel, pairHistoryReport.player);
        const partnerCandidates = pairHistoryReport.partnerCandidates;
        if (furtherRemainingPlayers.length > 0) {
            const partner = this.wheel.spin(partnerCandidates);
            pair.push(partner);
            this.spinAndAddPlayerUntilNoPlayersRemain(this.filterOutPlayer(furtherRemainingPlayers, partner), pairs, pairingRule);
        }
    }

    private spinAndAddPlayerUntilNoPlayersRemain(playersOnWheel, pairs, pairingRule) {
        if (playersOnWheel.length == 0) {
            return;
        }
        const pairHistoryReport = this.sequencer.getNextInSequence(playersOnWheel, pairingRule);
        const pair = [pairHistoryReport.player];
        pairs.push(pair);

        this.spinForAPartner(playersOnWheel, pairHistoryReport, pair, pairs, pairingRule);
    }

    public play(playersRoster, pairingRule) {
        const pairs = [];
        this.spinAndAddPlayerUntilNoPlayersRemain(playersRoster, pairs, pairingRule);
        return pairs;
    };
};