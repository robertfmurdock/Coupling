var CouplingGame = function CouplingGame(spinThatWheel) {


    function spinAndAddPlayerUntilNoPlayersRemain(playersOnWheel, pairCollector) {
        if (playersOnWheel.length == 0) {
            return;
        }

        var chosenPlayer = spinThatWheel(playersOnWheel);

        pairCollector.addToNextPair(chosenPlayer);

        var furtherRemainingPlayers = playersOnWheel.filter(function (player) {
            return player != chosenPlayer;
        });

        spinAndAddPlayerUntilNoPlayersRemain(furtherRemainingPlayers, pairCollector)
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
                },
                getFinalPairs: function () {
                    if (this.nextPair) {
                        this.pairs.push(this.nextPair);
                    }
                    return this.pairs;
                }
            };

            spinAndAddPlayerUntilNoPlayersRemain(playersRoster, pairingResults);
            return  pairingResults.getFinalPairs();
        }
    };
};
module.exports = CouplingGame;