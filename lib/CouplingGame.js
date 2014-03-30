var CouplingGame = function CouplingGame(spinThatWheel) {


    function spinAndAddPlayerUntilNoPlayersRemain(playersOnWheel, pairCollector) {
        var chosenPlayer = spinThatWheel(playersOnWheel);

        pairCollector.addToNextPair(chosenPlayer);

        var furtherRemainingPlayers = playersOnWheel.filter(function (player) {
            return player != chosenPlayer;
        });
        if (furtherRemainingPlayers.length > 0) {
            spinAndAddPlayerUntilNoPlayersRemain(furtherRemainingPlayers, pairCollector)
        }
    }

    return  {
        play: function (playersRoster) {

            var pairingResults = {
                pairs: [],

                addToNextPair: function (chosenPlayer) {
                    if (this.nextPair) {
                        this.nextPair.push(chosenPlayer);
                    } else {
                        this.nextPair = [chosenPlayer];
                    }

                    if (this.nextPair.length == 2) {
                        this.pairs.push(this.nextPair);
                        this.nextPair = null;
                    }
                }
            };

            spinAndAddPlayerUntilNoPlayersRemain(playersRoster, pairingResults);
            return  pairingResults.pairs;
        }
    };
};
module.exports = CouplingGame;