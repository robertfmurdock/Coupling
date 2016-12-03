var comparators = require("./Comparators").default;
var CouplingGame = function (sequencer, wheel) {

    function filterOutPlayer(playersOnWheel, playerToFilter) {
        return playersOnWheel.filter(function (player) {
            return !comparators.areEqualPlayers(player, playerToFilter);
        });
    }

    function spinForAPartner(playersOnWheel, pairHistoryReport, pair, pairs) {
        var furtherRemainingPlayers = filterOutPlayer(playersOnWheel, pairHistoryReport.player);
        var partnerCandidates = pairHistoryReport.partnerCandidates;
        if (furtherRemainingPlayers.length > 0) {
            var partner = wheel.spin(partnerCandidates);
            pair.push(partner);
            spinAndAddPlayerUntilNoPlayersRemain(filterOutPlayer(furtherRemainingPlayers, partner), pairs);
        }
    }

    function spinAndAddPlayerUntilNoPlayersRemain(playersOnWheel, pairs) {
        if (playersOnWheel.length == 0) {
            return;
        }
        var pairHistoryReport = sequencer.getNextInSequence(playersOnWheel);
        var pair = [pairHistoryReport.player];
        pairs.push(pair);

        spinForAPartner(playersOnWheel, pairHistoryReport, pair, pairs);
    }

    this.wheel = wheel;
    this.play =
        function (playersRoster) {
            var pairs = [];
            spinAndAddPlayerUntilNoPlayersRemain(playersRoster, pairs);
            return  pairs;
        };
};

module.exports = CouplingGame;