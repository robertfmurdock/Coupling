var CouplingGame = function (sequencer, wheel) {

    function filterOutPlayer(playersOnWheel, playerToFilter) {
        return playersOnWheel.filter(function (player) {
            return player != playerToFilter;
        });
    }

    function spinForAPartner(playersOnWheel, pairHistoryReport, pair, pairCollector) {
        var furtherRemainingPlayers = filterOutPlayer(playersOnWheel, pairHistoryReport.player);
        var partnerCandidates = pairHistoryReport.partnerCandidates;
        if (furtherRemainingPlayers.length > 0) {
            var partner = wheel.spin(partnerCandidates);
            pair.push(partner);
            spinAndAddPlayerUntilNoPlayersRemain(filterOutPlayer(furtherRemainingPlayers, partner), pairCollector);
        }
    }

    function spinAndAddPlayerUntilNoPlayersRemain(playersOnWheel, pairCollector) {
        if (playersOnWheel.length == 0) {
            return;
        }
        var pairHistoryReport = sequencer.getNextInSequence(playersOnWheel);
        var chosenPlayer = pairHistoryReport.player;
        var pair = [chosenPlayer];
        pairCollector.pairs.push(pair);

        spinForAPartner(playersOnWheel, pairHistoryReport, pair, pairCollector);
    }

    this.wheel = wheel;
    this.play =
        function (playersRoster) {
            var pairingResults = {
                pairs: []
            };
            spinAndAddPlayerUntilNoPlayersRemain(playersRoster, pairingResults);
            return  pairingResults.pairs;
        };
};

CouplingGame.prototype = {};
module.exports = CouplingGame;