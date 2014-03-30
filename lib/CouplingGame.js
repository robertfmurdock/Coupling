var CouplingGame = function CouplingGame(spinThatWheel) {

    function filterOutPlayer(playersOnWheel, playerToFilter) {
        return playersOnWheel.filter(function (player) {
            return player != playerToFilter;
        });
    }

    function spinForAPartner(playersOnWheel, chosenPlayer, pair, pairCollector) {
        var furtherRemainingPlayers = filterOutPlayer(playersOnWheel, chosenPlayer);

        if (furtherRemainingPlayers.length > 0) {
            var partner = spinThatWheel(furtherRemainingPlayers);
            pair.push(partner);

            spinAndAddPlayerUntilNoPlayersRemain(filterOutPlayer(furtherRemainingPlayers, partner), pairCollector);
        }
    }

    function spinAndAddPlayerUntilNoPlayersRemain(playersOnWheel, pairCollector) {
        if (playersOnWheel.length == 0) {
            return;
        }
        var chosenPlayer = spinThatWheel(playersOnWheel);

        var pair = [chosenPlayer];
        pairCollector.pairs.push(pair);

        spinForAPartner(playersOnWheel, chosenPlayer, pair, pairCollector);
    }

    return  {
        play: function (playersRoster) {

            var pairingResults = {
                pairs: []
            };

            spinAndAddPlayerUntilNoPlayersRemain(playersRoster, pairingResults);
            return  pairingResults.pairs;
        }
    };
};
module.exports = CouplingGame;