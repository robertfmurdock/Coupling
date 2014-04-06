var CouplingGame = function (wheel) {

    function filterOutPlayer(playersOnWheel, playerToFilter) {
        return playersOnWheel.filter(function (player) {
            return player != playerToFilter;
        });
    }

    function spinForAPartner(playersOnWheel, chosenPlayer, pair, pairCollector) {
        var furtherRemainingPlayers = filterOutPlayer(playersOnWheel, chosenPlayer);
        if (furtherRemainingPlayers.length > 0) {
            var partner = wheel.spin(furtherRemainingPlayers);

            pair.push(partner);

            spinAndAddPlayerUntilNoPlayersRemain(filterOutPlayer(furtherRemainingPlayers, partner), pairCollector);
        }
    }

    function spinAndAddPlayerUntilNoPlayersRemain(playersOnWheel, pairCollector) {
        if (playersOnWheel.length == 0) {
            return;
        }
        var chosenPlayer = wheel.spin(playersOnWheel);
        var pair = [chosenPlayer];
        pairCollector.pairs.push(pair);

        spinForAPartner(playersOnWheel, chosenPlayer, pair, pairCollector);
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